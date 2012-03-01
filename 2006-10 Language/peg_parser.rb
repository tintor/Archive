require 'peg'

class Object
  def class? a
    kind_of? Hash and self[:class]==a
  end
end

class Parser
  def initialize
    @peg = PEG.new
  
    @peg.add :Program, :Commands, :eof do |b| b[:code] end

    @peg.add :Commands, :Command, :Commands do |a,b| {:class=>:block, :code=>[a]+b[:code]} end
    @peg.add :Commands, :Command do |*a| {:class=>:block, :code=>a} end

    @peg.add :Command, :start.ignore, [:Assignments, :Definition].choice, :end.ignore
    
    # Function Definition
    @peg.add :Definition, :DefArgs, :'as'.ignore, :Assignments do |d,c| d.merge :code=>c end
    @peg.add :Definition, :DefArgs, :Commands do |d,c| d.merge :code=>c[:code] end

    @peg.add :DefArgs, :Def, :NameList do |d, *args| d.merge :arguments=>args end
    @peg.add :DefArgs, :Def do |d| d.merge :arguments=>[] end
    @peg.add :Def, :def.ignore, :Name do |name| {:class=>:function, :name=>name} end

    @peg.add :Assignments, :Assignment, [:';', :Assignments].optional
    
    @peg.add :Assignment, :AssignmentBase, [:if, :while].choice, :Expr do |a,m,e| {:class=>m[:class], :condition=>e, :do=>a} end
    @peg.add :Assignment, :AssignmentBase
    
    @peg.add :AssignmentBase, :NameList, :assignment, :ExprList do |n,a,e|
      {:class=>:assignment, :left=>n, :operator=>a[:value], :right=>e}
    end
    @peg.add :AssignmentBase, :ExprList
    
    @peg.add :NameList, :Name, [:','.ignore, :NameList].optional
    
    @peg.add :ExprList, :Expr, [:','.ignore, :ExprList].optional
    @peg.add :Exprs, :Expr, :Exprs.optional
    
    @peg.add :Expr, :Expr, [:and, :or].choice, :Expr1 do |a,op,b| {:class=>:binary, :left=>a, :operator=>op[:class], :right=>b} end
    @peg.add :Expr, :Expr1
    
    @peg.add :Expr1, :Expr1, :relational, :Expr2 do |a,op,b| {:class=>:binary, :left=>a, :operator=>op[:value], :right=>b} end
    @peg.add :Expr1, :Expr2
    
    @peg.add :Expr2, :Expr2, :addition, :Expr3 do |a,op,b| {:class=>:binary, :left=>a, :operator=>op[:value], :right=>b} end
    @peg.add :Expr2, :Expr3
    
    @peg.add :Expr3, :Expr3, :multiplication, :Dot do |a,op,b| {:class=>:binary, :left=>a, :operator=>op[:value], :right=>b} end
    @peg.add :Expr3, :Dot

    # Dot operator
    @peg.add :Dot, :Dot, :nospace, :'.'.ignore, :Name do |a,b| {:class=>:dot, :left=>a, :right=>b} end
    @peg.add :Dot, :Atom

    # Indexing
    @peg.add :Atom, :Atom, :nospace, :'['.ignore, :ExprList.optional, :']'.ignore do |a, *e| {:class=>:index, :base=>a, :index=>e} end
    @peg.add :Atom, :Atom, :nospace, :'['.ignore, :Exprs.optional, :']'.ignore do |a, *e| {:class=>:index, :base=>a, :index=>e} end
    
    # Special constaints
    @peg.add :Atom, [:true, :false, :nil].choice

    # Integer, String, Symbol, Regexp
    @peg.add :Atom, :integer do |a| a[:value] end
    @peg.add :Atom, :string do |a| a[:value] end
    @peg.add :Atom, :':'.ignore, :Name do |a| {:class=>:symbol, :value=>a} end
    @peg.add :Atom, :regexp do |a| {:class=>:regexp, :value=>a} end

    # Call
    @peg.add :Atom, :Name, :nospace, :'('.ignore, :ExprList, :')'.ignore do |name, *args| {:class=>:call, :name=>name, :args=>args} end
    @peg.add :Atom, :Name, :space, :ExprList do |name, *args| {:class=>:call, :name=>name, :args=>args} end
    @peg.add :Atom, :Name

    # Array
    @peg.add :Atom, :'['.ignore, :Exprs.optional, :']'.ignore do |*a| {:class=>:array, :value=>a} end
    @peg.add :Atom, :'['.ignore, :ExprList.optional, :']'.ignore do |*a| {:class=>:array, :value=>a} end
    @peg.add :Atom, :'['.ignore, :']'.ignore do {:class=>:array, :value=>[]} end
    
    # Inline Function
    @peg.add :Atom, :'{', [:NameList, '|'].optional, :Assignments, :'}'
    # List
    @peg.add :Atom, :'(', :ExprList, :')'
    # Hash
    @peg.add :Atom, :'{', :KeyValueList.optional, :'}'
    @peg.add :KeyValueList, :Expr, [:'=', :Expr].optional, [:',', :KeyValueList].optional
    
    # If
    @peg.add :Atom, :IfDo, :Else do |i,e| i.merge :else=>e[:code] end
    @peg.add :Atom, :IfDo
    @peg.add :IfDo, :If, :Commands do |i,c| i.merge :'do'=>c[:code] end

    @peg.add :Atom, :IfDoInline, :else.ignore, :ExprList do |i,a| i.merge :else=>a end
    @peg.add :Atom, :IfDoInline
    @peg.add :IfDoInline, :If, :'do'.ignore, :ExprList do |i,a| i.merge :'do'=>a end

    @peg.add :If, :if.ignore, :Expr do |e| {:class=>:if, :condition=>e} end
    @peg.add :Else, :end.ignore, :start.ignore, :else.ignore, :Commands

    # While
    @peg.add :Atom, :While, :'do'.ignore, :ExprList 
    @peg.add :Atom, :While, :Commands
    @peg.add :While, [:while, :until].choice, :Expr do |w, e| {:class=>:while} end
    
    # Case
    @peg.add :Atom, :case, :Expr, :When, :Else.optional
    @peg.add :When, :end, :start, :when, :ExprList, :Commands, :When.optional
    
    # With
    @peg.add :Atom, :with, :ExprList, :Commands
    
    # Name
    @peg.add :Name, :name do |a| a[:value] end
  end

  def parse tokens
    @peg.parse(tokens)[0][:Program]
  end
end

def parse tokens
  Parser.new.parse tokens
end