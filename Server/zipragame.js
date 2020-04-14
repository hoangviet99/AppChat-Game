//================================ ZIPRA ==================================
//============================= SERVER GAME ===============================

var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
var fs = require("fs");
server.listen(process.env.PORT || 3508);

app.get("/", function(req, res){
	res.sendFile(__dirname + "/index.html");	
});

console.log("Zipra Server Game Running!");

var coor_x, coor_y;
var idPlayer = 55555;
var arrPlayers = [];
var arrBomb = [];

io.sockets.on('connection', function(socket){
	console.log("Player Connected!");

	socket.on('Client-request-Login', function(username, coor_x, coor_y, characterIndex){
		arrPlayers.push(idPlayer + "|" + username + "|" + coor_x + "|" + coor_y + "|" + 1 + "|" + characterIndex);
		socket.username = username;
		console.log(username + " have login successful");
		io.sockets.emit('Server-reply-Login', { data: arrPlayers });
		idPlayer++;
	});

	socket.on('Client-request-Moving', function(id, username, x, y, moveDirection, characterIndex){
		for(var i = 0; i < arrPlayers.length; i++){
			var string = arrPlayers[i];
			var strings = [];
			strings = string.split("|");
			if(strings[0] == id.toString()){
				arrPlayers.splice(i, 1, id + "|" + username + "|" + x + "|" + y + "|" + moveDirection + "|" + characterIndex);
				io.sockets.emit('Server-reply-Moving', { data: arrPlayers[i] });
			}
		}
	});

	socket.on('Client-request-Animation', function(id, moveDirection, characterIndex){
		io.sockets.emit('Server-reply-Animation', { data: id + "|" + moveDirection + "|" + characterIndex });
	});

	socket.on('Client-request-Set-Bomb', function(username, x, y){
		arrBomb.push(username + "|" + x + "|" + y);
		io.sockets.emit('Server-reply-Set-Bomb', { data: username + "|" + x + "|" + y });
	});

	socket.on('Client-request-Dead', function(id, moveDirection, characterIndex){
		io.sockets.emit('Server-reply-Dead', { data: id + "|" + moveDirection + "|" + characterIndex });
	});

	socket.on('Client-request-Immortal', function(name){
		io.sockets.emit('Server-reply-Immortal', { data: name });
	});

	socket.on('Client-request-Chat', function(data, coor_x, coor_y){
		io.sockets.emit('Server-reply-Chat', { username: socket.username, data: data, coor_x: coor_x, coor_y: coor_y });
	});

	socket.on('disconnect', function(){
		for(var i = 0; i < arrPlayers.length; i++){
			var string = arrPlayers[i];
			var strings = [];
			strings = string.split("|");
			if(strings[1] == socket.username){
				arrPlayers.splice(i, 1);
				console.log(socket.username + " had logout!");
			}
		}
		io.sockets.emit('Server-reply-Login', { data: arrPlayers });
	});
});