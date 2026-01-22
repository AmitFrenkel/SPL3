#pragma once
#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include <tuple>
#include "../include/event.h"
#include <regex>

class Parser
{
private:
    std::string command;
    std::vector<std::string> args;
    std::vector<std::string> splitByChar(std::string& str, char charch = ' ');
    std::string user;
public:
    void parseInput(std::string& input);
    std::string createConnectFrame();
    std::string getCommand();
    std::pair<std::string, std::string> getHostAndPort();
    std::vector<std::string> createSendFrames();
    std::string parseGameUpdates(const std::map<std::string, std::string>& updates);
    void sortEventsByTime(std::vector<Event>& events);
    std::string getUser();
    std::string getPass();
    std::string getChannelName();
    std::string GetFileName();
    std::string trim_regex(const std::string& str);
    Parser();
};
