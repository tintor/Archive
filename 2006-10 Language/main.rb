PRIORITY = {:left=>0, :operator=>1, :right=>2, :condition=>0, :do=>1, :else=>2,
  :line=>200, :name=>0, :arguments=>1, :code=>2, :first=>0, :rest=>1}
PRIORITY.default = 100

def code_to_s code, indent=0, inline=false
  s = inline ? '' : "\t"*indent

  if code.kind_of? Hash
    s += code[:class].to_s + "\n"
    code.keys.select{|k| k != :class}.sort_by{|k| PRIORITY[k]}.each do |key|
      s += "\t"*(indent+1) + key.to_s + ' = '
      s += code_to_s code[key], indent+1, true
    end
  elsif code.kind_of? Array
    s += "-\n"
    code.each do |e|
      s += code_to_s e, indent+1, false
    end
  elsif code.kind_of? Symbol
    s += code.to_s + "\n"
  else
    s += code.inspect + "\n"
  end
  
  return s
end

def compact code
  code.each do |e|
    print '#', e[:line], "\t", e[:class]
    e.keys.select{|k| k != :class and k != :line}.sort_by{|k| PRIORITY[k]}.each do |key|
      print "\t"
      if key == :line
        print "##{e[key]}"
      else
        print key, '=', e[key].inspect
      end
    end
    puts
  end
end

require 'scanner'
require 'peg_parser'
require 'interpreter'

#lines = File.open "sample"
lines = File.open 'sample2'
tokens = scan lines

#print code_to_s(tokens)
#compact tokens

code = parse(tokens)[:result]
print code_to_s(tokens)
print code_to_s(code)

vm = VirtualMashine.new
puts '>> ' + vm.execute(code).inspect
p vm