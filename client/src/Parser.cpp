#include "../include/Parser.h"

    Parser::Parser() : command(""), args(), user("") {
    }   
    void Parser::parseInput(std::string& input) {
        input = trim_regex(input);
        args = splitByChar(input);
        command = args[0];
    }

    std::vector<std::string> Parser::splitByChar(std::string& str , char charch) {
        std::vector<std::string> words;
        std::stringstream stringstri(str); 
        std::string word;
        while (std::getline(stringstri, word, charch)) {
            words.push_back(word);
        }  
        return words;
    }

    std::string Parser::createConnectFrame() {
        user = args[2];
       return "CONNECT\naccept-version:1.2\nhost:"+splitByChar(args[1], ':')[0]+"\nlogin:"+user+
        "\npasscode:"+args[3]+"\n\n";
    

    }


    std::vector<std::string> Parser::createSendFrames(){
        names_and_events nae = parseEventsFile(args[1]);
        std::string destination = nae.team_a_name+"_"+nae.team_b_name+"\n";
        std::vector<Event> events = nae.events;
        sortEventsByTime(events);
        std::vector<std::string> frames;
        std::vector<std::string> fileNameSplit = splitByChar(args[1], '/');
        std::string fileName=fileNameSplit[fileNameSplit.size()-1];         
        for (size_t i = 0; i < events.size(); i++)
        {
            std::string frame = "SEND\ndestination:/"+destination+"fileName:"+fileName+"\n\n"+"user:"+user+"\n"+"team a: "+
            events[i].get_team_a_name()+"\n"+"team b: "+events[i].get_team_b_name()+"\n"+
            "event name: "+events[i].get_name()+"\n"+"time: "+std::to_string(events[i].get_time())
            +"\ngeneral game updates:\n"+parseGameUpdates(events[i].get_game_updates())+"team a updates:\n"+
            parseGameUpdates(events[i].get_team_a_updates())+"team b updates:\n"+parseGameUpdates(events[i].get_team_b_updates())+
            "description:\n"+events[i].get_discription()+"\n";
            frames.push_back(frame);   
        }
    return frames;
    }

    std::string Parser::parseGameUpdates(const std::map<std::string, std::string>& updates){
        std::string updatesStr = "";
        for (const auto& update : updates){
            updatesStr += update.first + ": " + update.second + "\n";
        }
        return updatesStr;
    }

    
    std::string Parser::getUser(){
        user = args[2];
        return user;
    }

    std::string Parser::getPass(){
        return args[3];
    }

    std::string Parser::getChannelName(){
        return args[1];
    }

    std::string Parser::GetFileName(){
        return args[3];
    }

    

    std::string Parser::getCommand() {
        return command;
    }
    std::pair<std::string, std::string> Parser::getHostAndPort() {
        std::string host = "";
        std::string port = "";
        std::vector<std::string> hostPort = splitByChar(args[1], ':');
        if (hostPort.size() == 2) {
            host = hostPort[0];
            port = hostPort[1];
        }
        return std::make_pair(host, port);
    }

    void Parser::sortEventsByTime(std::vector<Event>& events) {
        std::sort(events.begin(), events.end(), 
            [](const Event& a, const Event& b) {
                return a.get_time() < b.get_time();
            }
        );
    }

    std::string Parser::trim_regex(const std::string& str) {
    const std::regex pattern{R"(^\s+|\s+$)"}; 
    return std::regex_replace(str, pattern, "");
    }

    