# Rule interface
# def match position

$IGNORE = Object.new

$rules = Hash.new
def add name, *rules
  rule = case rules.size
  when 0: raise
  when 1: rules[0]
  else choice(*rules)
  end

  $rules[name] = {:rule=>rule}
  $rules[$last_rule][:next] = name if $last_rule
  $last_rule = name
end

# private
def match rule, position
  return $m[position][rule.object_id] if $m[position].has_key? rule.object_id
  
  $m[position][rule.object_id] = if $implicit.has_key? rule.class
    $implicit[rule.class].call(rule, position) {|r, p| match(r, p)}
  elsif rule =~ Symbol
    if $special.has_key? rule
      $special[rule].call position {|r,p| match(r, p)}   
    else
      rule = $rule_name if rule == :_
      rule = $rules[$rule_name][:next] if rule == :_next
      match $rules[$rule_name][:rule], position
    end
  else
    rule.match(position) {|r,p| match(r, p)}
  end
end

def parse rule_name, source
  $rule_name = rule_name
  $source = source
  $m = Array.new(source.size+1) {Hash.new}
  match $rules[rule_name], 0
end

$implicit = Hash.new
def implicit klass, &matcher
  $implicit[klass] = matcher
end

$special = Hash.new
def special symbol, &matcher
  $special[symbol] = matcher
end