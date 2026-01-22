#include "../include/StompClient.h"


void StompClient::updateSummary(std::string report) {
	std::string channel = "";
    std::string destKey = "destination:";
    size_t destPos = report.find(destKey);
    if (destPos != std::string::npos) {
        size_t start = destPos + destKey.length();
        size_t end = report.find('\n', start);
        if (end != std::string::npos) {
            channel = report.substr(start, end - start);
        }
    }
    std::string user = "";
    std::string userKey = "user:";
    size_t userPos = report.find(userKey);
    if (userPos != std::string::npos) {
        size_t start = userPos + userKey.length();
        size_t end = report.find('\n', start);
        if (end != std::string::npos) {
            user = report.substr(start, end - start);
        }
    }
    if (channel.empty() || user.empty()) {
        std::cerr << "Error: Could not extract channel or user from report." << std::endl;
        return;
    }
    if (summaries.find(channel) == summaries.end()) {
        summaries[channel] = std::map<std::string, Summary>();
    }
    if (summaries[channel].find(user) == summaries[channel].end()) {
        summaries[channel][user] = Summary();
    }

    summaries[channel][user].updateSummary(report);
}
	

void StompClient::listenServerThreadFunc(ConnectionHandler* handler, std::atomic<bool>* connected, 
	StompProtocol*& protocol) {
	while (*connected)
	{
		std::string frame;
		if (!handler->getFrameAscii(frame, '\0')) {
			*connected = false;
			handler->close();
			break;
		}
		if (frame.find("receipt-id:") != std::string::npos)
		{
			protocol->handleReceipt(frame);
		}
		else{
			if (frame.find("MESSAGE") != std::string::npos)
			{
				updateSummary(frame);
			}
		}
		
	}
	
}

bool StompClient::connectToDB(ConnectionHandler*& handler, Parser& parser, StompProtocol*& protocol) {
	return protocol->connect(parser.getHostAndPort().first, parser.getUser(), parser.getPass());
}

ConnectionHandler* StompClient::login(std::string host, int port, Parser& parser, std::thread* &listenServerThread,
	std::atomic<bool>* connected, StompProtocol*& protocol) {
	ConnectionHandler* connectionHandler = new ConnectionHandler(host, port);
	if (!connectionHandler->connect()) {
		std::cerr << "Could not connect to server" << std::endl;
		return nullptr;
	}
	std::cout << "Connected to " << host << ":" << port << std::endl;
	*connected = true;
	protocol = new StompProtocol(connectionHandler);
	listenServerThread = new std::thread(&StompClient::listenServerThreadFunc, this, connectionHandler, connected, std::ref(protocol));
	connectToDB(connectionHandler, parser, protocol);
	return connectionHandler;
}

void StompClient::subscribeToChannel(Parser& parser, StompProtocol*& protocol) {
	protocol->subscribe(parser.getChannelName());
}

void StompClient::unsubscribeFromChannel(Parser& parser, StompProtocol*& protocol) {
	protocol->unsubscribe(parser.getChannelName());
}


void StompClient::reportUpdate(ConnectionHandler* handler, Parser& parser, StompProtocol*& protocol) {
	try{
		std::vector<std::string> frames = parser.createSendFrames();
		for (const std::string& frame : frames) {
			protocol->sendMessage(frame);
		}
	} catch (const std::exception& e) {
		disconnect(protocol);
	}

}

void StompClient::summary(ConnectionHandler* handler, Parser& parser) {
	std::map<std::string, Summary>& channelSummaries = summaries[parser.getChannelName()];
	Summary& userSummary = channelSummaries[parser.getUser()];
	std::ofstream file(parser.GetFileName());
    if (file.is_open()) {
        file << userSummary.toString();
        file.close();
    }
}


void StompClient::disconnect(StompProtocol*& protocol) {
	protocol->disconnect();
}

void StompClient::userInputThreadFunc(std::thread* &listenServerThread) {
	Parser parser;
	std::atomic<bool> connected(false);
	ConnectionHandler* handler = nullptr;
	StompProtocol* protocol = nullptr;
	while (1)
	{
		const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
		parser.parseInput(line);
		std::string command = parser.getCommand();
		if (command == "login") {
			if (!connected) {
				handler = login(parser.getHostAndPort().first, std::stoi(parser.getHostAndPort().second),
				parser, listenServerThread, &connected, protocol);
				if (handler != nullptr) {
					std::cout << "Login successful\n" << std::endl;
				}
			}	else {
				std::cout << "The client is already logged in, log out before trying again\n" << std::endl;
			}
		}
		if (connected){
			if (command == "join") {
				subscribeToChannel(parser, protocol);
			}
			else if (command == "exit")
			{
				unsubscribeFromChannel(parser, protocol);
			}
			else if (command == "report")
			{
				reportUpdate(handler, parser, protocol);
			}
			
			else if (command == "summary"){
				summary(handler, parser);
			}

			else if (command == "logout") {
				disconnect(protocol);
				listenServerThread->join();
            	std::cout << "Disconnected. Exiting...\n" << std::endl;
				delete handler;
				delete protocol;
            	break;
        	}
		}
	}
}


int main(int argc, char *argv[]) {
	StompClient client;
	std::thread* listenServerThread = nullptr;
	std::thread userInputThread(&StompClient::userInputThreadFunc, &client, std::ref(listenServerThread));
	userInputThread.join();
	delete listenServerThread;
	return 0;
}

