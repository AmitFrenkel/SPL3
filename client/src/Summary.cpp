#include "../include/Summary.h"


Summary::Summary() : teamA(""), teamB(""), active(false), beforeHalf(true),
 teamAScore(0), teamBScore(0), finalpoessionA(0), finalpoessionB(0), 
 gameEventReports("Game event reports:\n") {}

void Summary::updateSummary(const std::string& report) {
    std::string data = splitStringByString(report, "\n\n")[1];
    std::vector<std::string> lines = splitStringByString(data, "\n");
    teamA = lines[1].substr(lines[1].find(":") + 2);
    teamB = lines[2].substr(lines[2].find(":") + 2);
    gameEventReports += lines[4].substr(lines[4].find(":") + 2)+" - "+
    lines[3].substr(lines[3].find(":") + 2);
    std::string general = report.substr(report.find("general game updates:"),
        report.find("team a updates:") - report.find("general game updates:"));
    std::string teamAUpdates = report.substr(report.find("team a updates:"),
        report.find("team b updates:") - report.find("team a updates:"));
    std::string teamBUpdates = report.substr(report.find("team b updates:"), 
        report.find("description") - report.find("team b updates:"));
    std::string description = report.substr(report.find("description:")+1);
    size_t activeIndex = general.find("active:");
    if (activeIndex != std::string::npos)
    {
        if (general.substr(activeIndex + 8, 4) == "true") {
            active = true;
        } else {
            active = false;
        }  
    }
    size_t beforeHalfIndex = general.find("before halftime:");
    if (beforeHalfIndex != std::string::npos)
    {
        if (general.substr(beforeHalfIndex + 17, 4) == "true") {
            beforeHalf = true;
        } else {
            beforeHalf = false;
        }
    }
    size_t teamAScoreIndex = teamAUpdates.find("goals:");
    if (teamAScoreIndex != std::string::npos)
    {
        teamAScore = std::stoi(teamAUpdates.substr(teamAScoreIndex + 7, 
            teamAUpdates.find("\n", teamAScoreIndex) - (teamAScoreIndex + 7)));
    }
    size_t teamAPossessionIndex = teamAUpdates.find("possession:");
    if (teamAPossessionIndex != std::string::npos)
    {
        finalpoessionA = std::stoi(teamAUpdates.substr(teamAPossessionIndex +12, 
            teamAUpdates.find("%") - (teamAPossessionIndex +12)));
    }
    size_t teamBScoreIndex = teamBUpdates.find("goals:");
    if (teamBScoreIndex != std::string::npos)
    {
        teamBScore = std::stoi(teamBUpdates.substr(teamBScoreIndex + 7, 
            teamBUpdates.find("\n", teamBScoreIndex) - (teamBScoreIndex + 7)));
    }
    size_t teamBPossessionIndex = teamBUpdates.find("possession:");
    if (teamBPossessionIndex != std::string::npos)
    {
        finalpoessionB = std::stoi(teamBUpdates.substr(teamBPossessionIndex +12, 
            teamBUpdates.find("%") - (teamBPossessionIndex +12)));
    }
    gameEventReports += "\n\n"+description+"\n\n\n";

    
}


std::string Summary::toString() const {
    return teamA + " vs " + teamB + "\nGame stats:\nGeneral stats:\nactive: "+ 
    (active ? "true" : "false")+"\nbefore halftime: "+ (beforeHalf ? "true" : "false")+
    "\n"+teamA+" stats:\ngoals: "+std::to_string(teamAScore)+
    "\npossession: "+std::to_string(finalpoessionA)+"%\n"+teamB+" stats:\ngoals: "+
    std::to_string(teamBScore)+"\npossession: "+std::to_string(finalpoessionB)+"%\n"+
    gameEventReports;
}

std::vector<std::string> Summary::splitStringByString(const std::string& input, const std::string& delimiter) {
    std::vector<std::string> result;
    size_t start = 0;
    size_t end = input.find(delimiter);
    while (end != std::string::npos) {
        result.push_back(input.substr(start, end - start));
        start = end + delimiter.length();
        end = input.find(delimiter, start);
    }
    result.push_back(input.substr(start));
    return result;
}