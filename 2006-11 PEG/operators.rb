class Choice
  def initialize rules
    @rules = rules
  end
  
  def match position
    @rules.each do |rule|
      s, r = yield rule, position
      return s, r if s 
    end
    nil
  end
end

def choice *rules
  Choice.new rules
end

class Map
  def initialize rule, action
    @rule = rule
    @action = action
  end
  
  def match position
    s, r = yield @rule, position
    s ? [s, action.call(*r)] : nil
  end
end

def compress rules
  case rules.size
  when 0: raise
  when 1: rules[0]
  else rules
  end
end

def map *rules, &block
  Map.new compress(rules), block
end

class Star
  def initialize rule
    @rule = rule
  end
  
  def match position
    results = []
    size = 0
    while true
      s, r = yield @rule, position + size
      break if !s
      size += s
      results << r if r != $IGNORE
    end    
    [size, results]
  end
end

def star *rules
  Star.new compress(rules)
end

def plus *rules
  rule = compress(rules)
  [rule, star(rule)]
end

class Optional
  def initialize rule
    @rule = rule
  end
  
  def match position
    s, r = yield @rule, position
    s ? [s, r] : true
  end
end

def optional *rules
  Optional.new compress(rules)
end

class Predicate
  def initialize rule, bool
    @rule = rule
    @bool = bool
  end
  
  def match position
    s, _ = yield @rule, position
    !s != @bool
  end
end

def yes *rules
  Predicate.new compress(rules), true
end

def no *rules
  Predicate.new compress(rules), false
end