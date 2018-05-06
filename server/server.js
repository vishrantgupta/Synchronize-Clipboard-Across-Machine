var appPort = process.env.PORT || 5000;

var nodemailer = require('nodemailer');

var express = require('express'), app = express();
var http = require('http')
  , server = http.createServer(app)
  , io = require('socket.io').listen(server);
// var socket = require('socket.io-client')('http://localhost');
io.sockets.autoConnect = true;

app.get('/', function(req, res){
  res.status(403).send("Forbidden");
});

var transport = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    auth: {
        user: 'syncclipboard@gmail.com',
        pass: 'yqebxhrorbzyqvxs'
    }
});

console.log('SMTP Configured');

// Message object
var Message = function(){

    this.from = 'noreply@clipboardsync.com';
    this.to;
    this.subject = 'Clipboard Synchronization Secret Code';
    this.html = 'Your secret code is: ';
};

Date.prototype.addMinutes = function(h) {    
   this.setTime(this.getTime() + (h*60*1000)); 
   return this;   
}

var allConnections = [];

var User = function(email, token) {
    this.email = email.toLowerCase();
    this.token = token;
    this.requestedTime = new Date();
    this.expiry = new Date().addMinutes(24 * 60);
    
    this.connections = [];
    
    this.addConnection = function(socket) {
        
        var addFlag = false;
        
        for(var i = 0; i < this.connections.length; i++) {
            if(this.connections[i].id == socket.id) {
                addFlag = true;
                break;
            }
        }
        if(!addFlag) {
            this.connections.push(socket);
        }
    };
    
    this.numberOfConnections = function() {
        return connections.length;
    };
    
    this.removeConnection = function(socketId) {
        for(var i = 0; i < this.connections.length; i++) {
            if(this.connections[i].id == socketId) {
                // console.log("Removing connection " + socketId);
                this.connections.slice(i, 1);
                // i--;
                // break;
            }
        }
    }
    
};

function isValidEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

app.get('/requestToken', function(req, res){
    
    if(req.query.email != null) {
        var email = req.query.email.toLowerCase();
        
        if(!isValidEmail(email)) {
            res.status(200).send("Invalid email id");
            return;
        }
        
        // var sendToken = false;
        var index = -1;
        
        for(var i = 0; i < allConnections.length; i++) {
            
            var user = allConnections[i];
            
            if(user.email.toLowerCase() == email.toLowerCase()) {
                
                if(new Date(user.requestedTime).addMinutes(5) > new Date()) {
                    res.status(200).send("A token is already sent to your email address, please retry after 5 minutes");
                    return;
                } else {
                    allConnections.slice(i);
                    index = i;
                    // sendToken = true;
                }
                break;
            }
        }
        
        // if(sendToken) {
            
            var token = Math.floor(100000 + Math.random() * 900000);
            var td = new User(email.toLowerCase(), token);
            
            var message = new Message();
            message.to = email.toLowerCase();
            message.html = message.html + token;
            
            try {
                transport.sendMail(message, function(error){
                    if(error){
                        console.log("Error while sending email " + error);
                        res.status(200).send("Error occured, please retry");
                        return;
                    }

                    transport.close(); // close the connection pool
                });
                
                if(index != -1) {
                    allConnections[index] = td;
                } else {
                    allConnections[allConnections.length] = td;
                }
                
                if(completeConnectionQueue[email] != null) {
                    console.log("Removing email id " + email);
                    
                    var connections = completeConnectionQueue[email].connections;
                    console.log("Number of connections " + connections.length);
                    for(var i = 0; i < connections.length; i++) {
                        console.log("Disconnecting forcefully.");
                        connections[i].disconnect();
                        console.log("Disconnected forcefully.");
                    }
                    
                    delete completeConnectionQueue[email];
                }
                
                for (var key in socketRepo) {
                    if (socketRepo.hasOwnProperty(key)) {
                        if(socketRepo[key] == email) {
                            console.log("Removing connections " + email);
                            delete socketRepo[key];
                        }
                    }
                }
                
                res.send('Your verification code is sent to your email id');
                return;
            } catch (err) {
                console.log("Error " + err);
            }
            
            res.status(200).send("Error occured, please retry");
        
    } else {
        res.status(200).send("Bad request");
        return;
    }
    
});

server.listen(appPort);
console.log("Server listening on port " + appPort);

// Handle the socket.io connections

var completeConnectionQueue = new Object();
var socketRepo = new Object();

io.sockets.on('connection', function (socket) { // First connection
	
    socket.on('handshake', function (emailid, token) {
        
        var handShakeComplete = false;
        
        if(emailid != null && token != null) {
            for(var i = 0; i < allConnections.length; i++) {
                if(allConnections[i].email.toLowerCase() == emailid.toLowerCase()) {
                    if(allConnections[i].token == token) {
                        if(allConnections[i].expiry > new Date()) {
                            allConnections[i].addConnection(socket);
                            
                            socketRepo[socket.id] = allConnections[i].email;
                            completeConnectionQueue[emailid.toLowerCase()] = allConnections[i];
                            
                            handShakeComplete = true;
                        } else {
                            socket.
                            // io.sockets.
                            emit('message', 'Your secret code is expired, generate it again');
                            socket.disconnect();
                        }
                    } else {
                        socket.
                        // io.sockets.
                        emit('message', 'Invalid token');
                        socket.disconnect();
                    }
                    break;
                }
            }
        }
        if(!handShakeComplete && socket.connected) {
            socket.emit('message', 'Please request secret code and try to reconnect');
            socket.disconnect();
        }
        
    });
    
    socket.on('inform', function(content) {
        
        if(socketRepo[socket.id] != null && completeConnectionQueue[socketRepo[socket.id]] != null) {
            var sockets = completeConnectionQueue[socketRepo[socket.id]].connections;
            
            for(var i = 0; i < sockets.length; i++) {
                if(sockets[i].id != socket.id) {
                    sockets[i].emit("update", content);
                }
            }
            
        }
        
        content = null;
        
    });
    
	socket.on('disconnect', function () { // Disconnection of the client
        
        if(socketRepo[socket.id] != null && completeConnectionQueue[socketRepo[socket.id]] != null) {
            var user = completeConnectionQueue[socketRepo[socket.id]];
            
            // console.log("Number of connections " + user.email);
            
            user.removeConnection(socket.id);
            if(user.numberOfConnections == 0) {
                delete completeConnectionQueue[socketRepo[socket.id]];
            }
            
        }/* else {
            console.log("Not removed from list");
        } */
        
        
        console.log("Disconnected...");
	});
});

setInterval(function() {
    console.log("Running");
}, 300000); // every 5 minutes (300000)
