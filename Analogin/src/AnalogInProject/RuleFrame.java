package AnalogInProject;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;

public class RuleFrame extends JFrame {

	private JPanel contentPane;
	public JTextField textField;

	/**
	 * Create the frame.
	 */
	public RuleFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(84, 110, 264, 47);
		contentPane.add(textField);
		textField.setColumns(10);
		textField.setText(GIM.rule);
		
		JLabel lblWhatIsYour = new JLabel("What is your RULE?");
		lblWhatIsYour.setFont(new Font("Times New Roman", Font.BOLD, 16));
		lblWhatIsYour.setBounds(142, 45, 191, 54);
		contentPane.add(lblWhatIsYour);
		
		JButton btnEnter = new JButton("ENTER");
		btnEnter.setFont(new Font("Times New Roman", Font.BOLD, 12));
		btnEnter.setBounds(169, 167, 97, 23);
		contentPane.add(btnEnter);
		btnEnter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GIM.rule = textField.getText();
				dispose();
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}