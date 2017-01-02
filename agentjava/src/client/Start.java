/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package client;

import jade.MicroBoot;
import jade.core.MicroRuntime;
import jade.util.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import client.helper.ProductHelper;
import client.model.ProductResponse;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Start extends MicroBoot
{
	public static void main(String args[])
	{
		MicroBoot.main(args);
		ObjectMapper mapper = new ObjectMapper();
		ProductResponse products = ProductHelper.CreateDefaultProducts();

		try
		{
			File file = new File("D:\\produse.json");
			if (file.exists() && !file.isDirectory())
			{
				mapper.writeValue(file, products);
			}
			else
			{
				mapper.writeValue(new File("D:\\produse.json"), products);
			}

		}
		catch (JsonGenerationException e)
		{
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		StartDialog dialog = new StartDialog("StartAgents");
	}

	private static class StartDialog extends Frame implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private Logger logger = Logger.getMyLogger(this.getClass().getName());
		private Button startButton;
		StartDialog(String s)
		{
			super(s);
			setSize(getProperSize(256, 320));
			startButton = new Button("Start Agents");
			startButton.addActionListener(this);
			Panel p = new Panel();
			p.setLayout(new BorderLayout());
			p.add(startButton, BorderLayout.CENTER);
			this.add(p, BorderLayout.CENTER);
			addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					MicroRuntime.stopJADE();
				}
			});

			showCorrect();
			this.setVisible(true);
		}

		public void actionPerformed(ActionEvent e)
		{
			try
			{
				MicroRuntime.startAgent("displayAgent", "client.agent.DisplayAgent", null);
				MicroRuntime.startAgent("filePathInfoAgent", "client.agent.FilePathInfoAgent", null);
				startButton.setEnabled(false);
				
			}
			catch (Exception ex)
			{
				logger.log(Logger.SEVERE, ex.toString());
			}

		}

		private void showCorrect()
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = getSize();
			int centerX = (int) screenSize.width / 2;
			int centerY = (int) screenSize.height / 2;
			setLocation(centerX - frameSize.width / 2, centerY - frameSize.height / 2);

		}

		private Dimension getProperSize(int maxX, int maxY)
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width < maxX ? screenSize.width : maxX);
			int y = (screenSize.height < maxY ? screenSize.height : maxY);
			return new Dimension(x, y);
		}
	}
}