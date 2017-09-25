try
{
	while(true)
	{
		//Accepting client connection
		Log.e("SERVER : ", "Connection Accepted");
		Socket respondedClientSocket = serverSocket.accept();
		
		//Getting what the client sent using inputstream
		InputStream input_stream = respondedClientSocket.getInputStream();
		InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
		BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
		
		//Message from client
		String msgToDisplay = buffered_reader.readLine();
		Log.e("SERVER : ", "Received the message to be displayed : "+msgToDisplay);
		
		//Sending acknowledgement to client through outputstream
		OutputStream output_stream = respondedClientSocket.getOutputStream();
		OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
		BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);
		
		//Acknowledgement message from server
		String msgToAck = "Got the package";
		Log.e("SERVER : ", "Acknowledging reception : "+msgToAck);
		buffered_writer.write(msgToAck);
		buffered_writer.flush();

		//Updating the UI by calling updateProgress from inBackground using publishProgress
		publishProgress(msgToDisplay);
		Log.e("SERVER : ", "Closing socket : "+msgToAck);
		respondedClientSocket.close();
	}
}
catch(IOException exp)
{
	exp.printStackTrace();
}

Client

//Using outputstream to write to server
Log.e("CLIENT : ", "Establishing connection with server");
OutputStream output_stream = socket.getOutputStream();
OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);
Log.e("CLIENT : ", "Sending message to server"+msgToSend);
buffered_writer.write(msgToSend);
buffered_writer.flush();

//Getting the acknowledgementmessage from server
Log.e("CLIENT : ", "Waiting to receive acknowledgement from server");
InputStream input_stream = socket.getInputStream();
InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
String msgReceived = buffered_reader.readLine();
Log.e("CLIENT : ", "Received acknowledgement from server");

//Checking if client had received the correct acknowledgement message
if(msgReceived == "Got the package")
{
	Log.e("CLIENT : ", "Closing socket");
	//Closing socket
	socket.close();
}

