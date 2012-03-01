require 'java'
 
frame = javax.swing.JFrame.new
frame.content_pane.add javax.swing.JLabel.new("This is an example.")
frame.pack()
frame.visible = true