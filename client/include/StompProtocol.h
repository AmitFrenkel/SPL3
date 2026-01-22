#pragma once

#include "../include/ConnectionHandler.h"
#include <string>
#include <map>
#include <iostream>
#include <sstream>
#include <algorithm>
#include "../include/event.h"



class StompProtocol
{
private:
    ConnectionHandler* handler;
    int subscribeId;
    int receiptId;
    std::map<std::string, int> channelToId;
    std::map<int, std::string> pendingReceipts;
public:
    StompProtocol(ConnectionHandler*& handler);
    StompProtocol(const StompProtocol&) = delete;
    StompProtocol& operator=(const StompProtocol&) = delete;
    bool connect(std::string hostPort, std::string user, std::string pass);
    void subscribe(std::string channel);
    void unsubscribe(std::string channel);
    void sendMessage(std::string msg);
    void disconnect();
    void handleReceipt(std::string receipt_frame);
};
