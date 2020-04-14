//================================ ZIPRA ==================================
//============================= SERVER CHAT ===============================

var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
var fs = require("fs");
server.listen(process.env.PORT || 8888);

app.get("/", function(req, res){
	res.sendFile(__dirname + "/index.html");	
});

console.log("Zipra Server Running!");

var arrUsernames = [];
var arrUsernames_LowerCase = [];
var isLoginSuccessful = true;
var isConnect = true;
var chatContent = [];

io.sockets.on('connection', function(socket){
	console.log("Device Connected!");

	socket.on('Client-Testing-Server', function(){
		socket.emit('Server-reply-Testing', { bool: isConnect });
	});

	socket.on('Client-ask-to-Login', function(data){
		if(arrUsernames_LowerCase.indexOf(data.toLowerCase()) == -1){
			isLoginSuccessful = true;
			socket.emit('Server-agree-Login', { bool: isLoginSuccessful });
		}
		else {
			isLoginSuccessful = false;
			socket.emit('Server-agree-Login', { bool: isLoginSuccessful });
		}
	});

	socket.on('Client-request-Login', function(data){
		arrUsernames.push(data);
		arrUsernames_LowerCase.push(data.toLowerCase());
		socket.username = data;
		console.log(arrUsernames[arrUsernames.length - 1] + " login successful!")
		io.sockets.emit('Server-reply-Login', { arrOnline: arrUsernames });
		socket.emit('Server-Welcome-Client', { username: "Server", welcome: "Bạn vừa tham gia phòng chat!" });
		socket.broadcast.emit('Server-Welcome-Client', { username: "Server", welcome: data + " vừa tham gia phòng chat!" });
	});

	socket.on('Client-request-Chatting', function(data){
		console.log("Username: " + socket.username + " || Chat: " + data);
		if(socket.username != null){
			chatContent.push(socket.username + "|" + data);
			io.sockets.emit('Server-reply-Chatting', { username: socket.username, chat: data });
		}
	});

	socket.on('Client-obtain-Chatting-Data', function(){
		socket.emit('Server-send-Chatting-Data', { chatData: chatContent });
	});

	socket.on('disconnect', function(){
		if(arrUsernames.indexOf(socket.username) > -1){
			arrUsernames.splice(arrUsernames.indexOf(socket.username),1);
			arrUsernames_LowerCase.splice(arrUsernames_LowerCase.indexOf(socket.username.toLowerCase()),1);
			io.sockets.emit('Server-reply-Login', { arrOnline: arrUsernames });
			socket.broadcast.emit('Client-Logout', { username: "Server", content: socket.username + " đã thoát khỏi phòng chat!" })
			console.log(socket.username + " had logout!");
		}
	});
});