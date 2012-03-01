require 'test/unit'
require 'scanner'

class MyScanner < Scanner
  attr_accessor :tokens, :line_number, :line, :problem, :class, :value
  
  def error problem
    @problem = problem
  end
  
  def token _class, value = nil
    @class = _class
    @value = value
  end
end

class ScannerTest < Test::Unit::TestCase
  def test_match
    check_match '35 + 10', ' + 10', :integer, 35
    check_match '35', '', :integer, 35
    check_match '12_354', '', :integer, 12354
    check_match '9_124_354', '', :integer, 9124354

#    check_match ' a', 'a', :space

    check_match_error '  a+2', 'multiple space characters'
    check_match_error '    ', 'trailing space'
  end

  def check_match_error line, problem
    m = MyScanner.new
    m.line = line
    m.class = nil
    m.value = nil

    assert_nothing_raised {m.match}
    
    assert_equal nil, m.class
    assert_equal nil, m.value
    assert_equal problem, m.problem
  end
  
  def check_match line, line_out, _class, value = nil
    m = MyTokenizer.new
    m.line = line
    m.class = nil
    m.value = nil
    
    assert_nothing_raised {m.match}
    
    assert_equal _class, m.class
    assert_equal value, m.value
    assert_equal line_out, m.line
  end
end