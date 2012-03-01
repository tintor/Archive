class Object
  def class? a
    kind_of? Hash and self[:class]==a
  end
end

class Scope
  def initialize
    @scope = [{}]
  end

  def push vars
    @scope.push vars
  end

  def pop
    @scope.pop
  end

  def has_key? key
    @scope.reverse_each {|e| return true if e.has_key? key}
    false
  end
  
  def [] key
    @scope.reverse_each {|e| return e[key] if e.has_key? key}
    nil
  end
  
  def []= key, value
    @scope.reverse_each {|e| return e[key] = value if e.has_key? key}
    @scope.last[key] = value
  end
  
  def inspect
    @scope.inspect
  end
end

class VirtualMashine
  def initialize
    @scope = Scope.new
  end
  
  def execute code
    case code
    when Array
      code.inject(nil) {|v,e| execute e}
    
    when Hash
      case code[:class]
      when :if
        execute execute(code[:condition]) ? code[:do] : code[:else]

      when :while
        execute code[:do] while execute code[:condition]
              
      when :assignment
        a = code[:left]
        b = execute code[:right]
        
        case code[:operator]
        when '+='
          raise "undefined symbol '#{a}'" unless @scope.has_key? a
          @scope[a] += b
        when '='
          @scope[a] = b
        else
          raise "invalid assignment operator '#{code[:operator]}'"
        end

      when :function
        @scope[code[:name]] = code

      when :call
        a = code[:args].map {|a| execute a}

        case code[:name]
        when :puts
          puts a[0]
          nil
        else
          raise "undefined function '#{code[:name]}'" unless @scope.has_key? code[:name]
          func = @scope[code[:name]]
          raise "'#{code[:name]}' is not a function" unless func.class? :function
          
          args = func[:arguments].zip(a).inject({}) {|h, e| h.merge e[0]=>e[1]}
          @scope.push args
          result = execute func[:code]
          @scope.pop
          result
        end
      
      when :binary
        a = execute code[:left]
        b = execute code[:right]

        case code[:operator]
        when '+': a + b 
        when '-': a - b
        when '*': a * b
        when '/': a.to_f / b.to_f
        when '//': a.to_i / b.to_i
        when '%': a % b
        when '>': a > b
        else raise "invalid operator '#{code[:operator]}'"
        end
    
      when :array
        code[:value].map {|e| execute e} 
      
      else
        raise "invalid class #{code[:class]}, #{code.inspect}"
      end
    
    when Symbol
      raise "undefined variable or function '#{code}'" unless @scope.has_key? code

      value = @scope[code]
      if value.class? :function
        @scope.push Hash.new
        value = execute value[:code]
        @scope.pop
      end
      value
      
    when Integer, String
      code

    when nil
    
    else
      raise "unexpected object #{code.class}"
    end
  end
  
  def inspect
    @scope.inspect
  end
end

def execute code
  VirtualMashine.new.execute code
end