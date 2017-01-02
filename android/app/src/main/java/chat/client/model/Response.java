package chat.client.model;

import java.util.List;

public class Response
{
	private String errorText;
	
	public Response()
	{
		errorText = "";
	}

	public String getErrorText()
	{
		return errorText;
	}

	public void setErrorText(String errorText)
	{
		this.errorText = errorText;
	}
}
