require 'English'

class ScannerError < Exception; end

class Scanner
  def scan lines
    @tokens = []
    indent = -1
    @line_number = 0

    lines.each do |@line|
      @line_number += 1

      # remove end of line
      @line.chomp!

      # skip empty lines
      next if @line =~ /^[ \t]*$/

      # if comment-only line
      if @line =~ /^[ \t]*#/
        token :comment, $POSTMATCH.strip
        next
      end

      @length = @line.size

      # handle indentation
      @line =~ /^\t*/
      @line = $POSTMATCH

      error 'High indentation. Remove some tabs.' if $MATCH.size > indent+1
      (indent-$MATCH.size+1).times {token :end}
      token :start
      indent = $MATCH.size

      @string_expr = false
      @space = false

      @state = :match
      send @state while @state != :end
    end

    # return to zero indentation
    (indent+1).times {token :end}
  
    @tokens
  end

  def match_string
    case @line
    when ''
      error 'unterminated string'
  
    when /^"/
      @line = $POSTMATCH
      token :string_end
      @state = :match

    when /^#\{/
      token :'#{'
      error 'nested string expression' if @string_expr
      @string_expr = true
      @state = :match
    
    when /^\\/
      @line = $POSTMATCH
      case @line
      when ''
        error 'unterminated string'
      when 'n'
        @line = $POSTMATCH
        token :char, "\n"
      when 't'
        @line = $POSTMATCH
        token :char, "\t"
      when '\\'
        @line = $POSTMATCH
        token :char, "\\"
      when '\"'
        @line = $POSTMATCH
        token :char, "\""
      else
        error 'invalid escape' 
      end
    
    when /^./
      @line = $POSTMATCH
      token :char, $MATCH
    end
  end
  
  def match
    case @line
    when ''
      @state = :end

    when /^( )?#/
      # check for at least two spaces before comment
      error 'two spaces are required before #'
        
    when /^[ \t]*  #/
      # comment
      token :comment, $POSTMATCH.strip
      @state = :end
        
    when /^\d+[A-Za-z]/, /^\d{1,3}(_\d\d\d)+[A-Za-z]/
      # check for name right after int
      error 'space is nessesary beetwen integer and name'

    when /^\d[0-9_]*/
      # integer
      @line = $POSTMATCH
      if $MATCH =~ /^(\d+|\d{1,3}(_\d\d\d)+)$/ 
        token :integer, $MATCH.gsub('_', '').to_i
      else
        error '_ must separate groups of 3 digits starting from end of number'
      end

    when /^[A-Za-z_][A-Za-z0-9_]*/
      # name or keyword
      @line = $POSTMATCH
      case $MATCH
      when 'do', 'as', 'if', 'else', 'while', 'def', 'and', 'or', 'xor', 'not'
        token $MATCH.to_sym
      else
        token :name, $MATCH.to_sym
      end

    when /^\s+$/
      # check for trailing space
      error 'trailing space'

    when /^  /
      # check for multiple space
      error 'multiple space characters'

    when /^ /
      # space
      @line = $POSTMATCH
      @space = true

    when /^\/([^\/]*)\//
      # regular expression
      @line = $POSTMATCH
      token :regexp, $LAST_MATCH_INFO[1]

    when /^'([^']*)'/
      # ' string
      @line = $POSTMATCH
      token :string, $LAST_MATCH_INFO[1]

    when /^"/
      @line = $POSTMATCH
      token :string_start
      @state = :match_string

    when /^\}/
      if @string_expr
        @string_expr = false
        @state = :match_string 
      end
      token :'}'

    when /^(>=?|<=?|==|!=)/
      # relational operator
      @line = $POSTMATCH
      token :relational, $MATCH

    when /^[\+\-\*\/%]?=/
      # assignment operator
      @line = $POSTMATCH
      token :assignment, $MATCH

    when /^[+-]/
      # addition operator
      @line = $POSTMATCH
      token :addition, $MATCH

    when /^\*|%|\/(\/?)/
      # multiplication operator
      @line = $POSTMATCH
      token :multiplication, $MATCH

    when /^[()\[\]{,.]/
      # misc
      @line = $POSTMATCH
      token $MATCH.to_sym

    else
      error 'invalid token'
    end
  end

  def error problem
    raise ScannerError, "#{@line_number}: #{problem} at: #{@line}"
  end

  def token _class, value = nil
    hash = {:class => _class, :line => @line_number}
    
    hash[:value] = value if value != nil
    
    if @space
      hash[:space] = true if _class != :start
      @space = false
    end
    
    @tokens << hash
  end
end

def scan lines
  Scanner.new.scan lines
end