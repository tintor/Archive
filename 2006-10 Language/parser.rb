class ParserError < Exception
end

class Parser
  def parse tokens
    @tokens = tokens
    @position = 0
    parse_block
  end

  def parse_block
    commands = []
    expect :start
    
    loop do
      if optional :if 
        line = @token[:line]
        condition = expect_expression
        expect :eol, 'end of line expected after IF condition'
        block = parse_block
        if optional :else
          expect :eol, 'end of line expected after ELSE'
          block2 = parse_block
        else
          block2 = {:class => :pass}
        end
        commands << {:class => :if, :line => line, :condition => condition, :do => block, :else => block2}
      
      elsif optional :else  
        error "ELSE without IF"
      
      elsif optional :def
        line = @result[:line]
        name = expect(:name)[:value]
        args = []
        if optional :name
          args << @result[:value]
          while optional :','
            args << expect(:name)[:value]
          end
        end
        expect :eol
        commands << {:class => :function, :line => line, :name => name, :args => args, :code => parse_block}
        
      elsif optional :while
        line = @result[:line]
        condition = expect_expression
        expect :eol, 'end of line expected after WHILE condition'
        block = parse_block
        commands << {:class => :while, :line => line, :condition => condition, :do => block}
      
      elsif optional :end
        break
      
      elsif optional_expression 0
        e = @result
        if optional :assignment
          e = {:class => :assignment, :line => @result[:line], :operator => @result[:value], :left => e, :right => expect_expression(0)}
        end
        expect :eol, 'end of line expected after expression'
        commands << e

      else
        expect nil
      end
    end
    
    commands
  end

  def optional_expression level=1
    position = @position
    begin
      @result = expect_expression level
      true
    rescue ParserError => e
#      puts "!! hidden #{e} !!"
      @position = position
      @result = nil
      false
    end
  end
  
  def expect_expression level=1
    @level = level-1
    expr
  end
  
  def expr
    level = @level
    @level += 1

    @result = case @level
    when 0
      # list
      a = [expr]
      while optional :','
        a << expr
      end
      a.size > 1 ? {:class => :array, :elements => a, :naked => true} : a[0]
    
    when 1
      # relational
      first = expr
      rest = []
      while optional :relational
        rest << [@result[:value], expr]
      end
      rest.size > 0 ? {:class => :relational, :first => first, :rest => rest} : first
    
    when 2
      # addition
      a = expr
      while optional :addition
        a = {:class => :binary, :operator => @result[:value], :line => @result[:line], :left => a, :right => expr}
      end
      a

    when 3
      # multiplication
      a = expr
      while optional :multiplication
        a = {:class => :binary, :operator => @result[:value], :line => @result[:line], :left => a, :right => expr}
      end
      a

    when 4
      # atom
      if optional :'['
        if optional :']'
          e = []
        else
          expect_expression
          e = (@result.kind_of? Hash and @result[:class] == :list) ? @result[:elements] : @result
          expect :']'
        end
        {:class => :array, :elements => e}
      elsif optional :'('
        e = expect_expression
        expect :')'
        e
      elsif optional :name
        e = @result[:value]
        if optional_expression
          e = {:class => :call, :name => e, :args => [@result]}
        elsif optional :'('
          line = @result[:line]
          if optional_expression
            arguments = [@result]
          else
            arguments = []
          end
          e = {:class => :call, :line => line, :name => e, :args => arguments}
          expect :')'
        end
        e
      elsif optional(:integer) or optional(:string)
        @result[:value]
      else
        expect nil
      end
    end

    @level = level
    @result
  end
    
  def optional token_class
    @result = @tokens[@position]
    if @result[:class] == token_class
      @position += 1
      return true
    else
      return false
    end
  end
  
  def error problem
    raise ParserError, "#{@tokens[@position][:line]}: #{problem}"
  end

  def expect token_class, message=nil
    s = "expected class #{token_class.inspect}, but got #{@result.inspect}"
    s += ", #{message}" if message
    error s if !optional token_class
    @result
  end
end

def parse tokens
  begin
    Parser.new.parse tokens
  rescue ParserError => e
    puts e.message
    nil
  end
end