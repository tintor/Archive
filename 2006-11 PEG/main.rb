require "peg.rb"
require "operators.rb"
require "extensions.rb"

add :expr,  :_next
add :rel,   binary_left('<=', '<', '>=', '>', '==', '!=')
add :add,   binary_left('+', '-')
add :mult,  binary_left('*', '//', '/', '%')
add :pow,   binary_right('**')
add :unary, ['-', :atom], :atom
add :atom,  :real, :int, :name, map('(', :expr, ')') {|_,a,_| a}
add :name,  %r{[a-zA-Z][0-9a-zA-Z_]*}
add :real,  map(%r{\d+\.\d+}) {|e| e.to_f} 
add :int,   map(%r{\d+}) {|e| e.to_i}

parse :expr, '2//(1+2*4)>0-0>-9'