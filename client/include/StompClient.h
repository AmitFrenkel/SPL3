#pragma once

#include <map>
#include <string>
#include <iostream>
#include <thread>
#include "../include/ConnectionHandler.h"
#include "../include/Parser.h"
#include "../include/Summary.h"
#include <fstream>
#include "../include/StompProtocol.h"
#include <atomic>

class StompClient{
    private:
        std::map<std::string, std::map<std::string, Summary>> summaries = std::map<std::string, std::map<std::string, Summary>>();
    public:
        void summary(ConnectionHandler* handler, Parser& parser);
        void updateSummary(std::string report);
        void disconnect(StompProtocol*& protocol);
        void listenServerThreadFunc(ConnectionHandler* handler, std::atomic<bool>* connected, StompProtocol*& protocol);
        bool connectToDB(ConnectionHandler*& handler, Parser& parser, StompProtocol*& protocol);
        ConnectionHandler* login(std::string host, int port, Parser& parser, 
            std::thread* &listenServerThread, std::atomic<bool>* connected, StompProtocol*& protocol);
        void subscribeToChannel(Parser& parser, StompProtocol*& protocol);
        void unsubscribeFromChannel(Parser& parser, StompProtocol*& protocol);
        void reportUpdate(ConnectionHandler* handler, Parser& parser, StompProtocol*& protocol);
        void userInputThreadFunc(std::thread* &listenServerThread);     
};