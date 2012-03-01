implicit Array do |elements, position|
  result = []
  size = 0
  elements.each do |e|
    s, r = yield(e, position+size)
    return nil if !s
    size += s    
    result << r unless r == $IGNORE
  end
  
  [size, case result.size
  when 0: $IGNORE
  when 1: result[0]
  else result
  end]
end

implicit String do |string, position|
  string == $source[position..position+string.size] ? [string.size, string] : nil
end

implicit Regexp do |regexp, position|
  regexp =~ $source[position..-1] ? [$MATCH.size, $MATCH] : nil
end

special :_eof do |position|
  position == $source.size
end

def ignore *rules; map(*rules) {$IGNORE} end

def binary_left *ops
  # choice([:_, choice(*ops), :_next], :_next)
  [:_next, star(choice(*ops), :_next)]  # left recursion is still not working
end

def binary_right *ops; choice([:_next, choice(*ops), :_], :_next); end