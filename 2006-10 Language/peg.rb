require 'English'

class Object
  def ignore
    {:class=>:ignore, :value=>self}
  end
  
#  def star
#    {:class=>:star, :value=>self}
#  end
#  
#  def plus
#    [self, star]
#  end
  
  def optional
    {:class=>:optional, :value=>self}
  end
  
  def yes
    {:class=>:predicate, :value=>self, :flip=>false}
  end
  
  def no
    {:class=>:predicate, :value=>self, :flip=>true}
  end
end

class Array
  def choice
    {:class=>:choice, :value=>self}
  end

  def each_reverse
    (length-1).downto(0) {|i| yield self[i]}
  end

  def each_index_reverse
    (length-1).downto(0) {|i| yield i}
  end
end

class PEG
  def initialize
    @rules = []
  end

  def add name, *matcher, &mapper
    matcher = matcher[0] if matcher.size == 1
    @rules << {:name=>name, :matcher=>matcher, :mapper=>mapper}   
  end

  def match matcher, position
    case matcher
    when Hash
      case matcher[:class]
      when :ignore
        x = match matcher[:value], position
        return x ? (x.merge :result => nil) : nil

      when :optional
        x = match matcher[:value], position
        return x ? x : {:size => 0, :result => nil}

      when :predicate
        x = match matcher[:value], position
        return (!x ^ matcher[:flip]) ? nil : {:size => 0, :result => nil}
        
#      when :star
#        result = []
#        loop do
#          p, r = match matcher[:value], position
#          break if !p
#          result << r if r != nil
#          p = position
#        end
#        [position, result]

      when :choice
        return matcher[:value].map{|e| match(e, position)}.find{|e| e}
            
      else
        raise matcher.inspect
      end
      
    when Array
      result = []
      size = 0
      matcher.each do |e|
        m = match e, position+size
        return nil unless m

        size += m[:size]
        result << m[:result] if m[:result] != nil
      end
      result.flatten!
      result = result[0] if result.size == 1
      return {:size=>size, :result=>result} 

    when Symbol
      case matcher
      when :eof
        return position == @source.length ? {:size=>0, :result=>nil} : nil
      
      when :nospace, :space
        return (matcher == :space) == (position < @source.length && @source[position][:space])  ? {:size=>0, :result=>nil} : nil
      
      else
        return nil if position >= @source.length
            
        if matcher.to_s == matcher.to_s.downcase
          return @source[position][:class] == matcher ? {:size=>1, :result=>@source[position]} : nil
        else
          return @m[position][matcher]
        end
      end

      
    when String
      return matcher == @source[position...position+matcher.size] ? {:size=>matcher.size, :result=>matcher.clone} : nil
      
    when Regexp
      return (matcher.match @source[position..-1] and $PREMATCH.size == 0) ? {:size=>$MATCH.size, :result=>$MATCH} : nil

    else
      raise "#{matcher.class} #{matcher.inspect}"
    end
  end
  private :match
  
  def parse source
    @source = source
    @m = Array.new(source.size) {Hash.new}

    @m.each_index_reverse do |position|
      priority = Hash.new
      again = true
      while again
        again = false
        @rules.each_index_reverse do |rule_index|
          rule = @rules[rule_index]
          old = @m[position][rule[:name]]

          new = match(rule[:matcher], position)
          if new and rule[:mapper]
            r = new[:result]
            new = new.merge :result => (r.kind_of?(Array) ? rule[:mapper].call(*r) : rule[:mapper].call(r))
          end

          if new != nil and new != old and (old == nil || rule_index <= priority[rule[:name]])
            @m[position][rule[:name]] = new
            priority[rule[:name]] = rule_index
            again = true
          end
        end
      end
    end
    
    @m
  end
end