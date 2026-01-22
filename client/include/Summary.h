#pragma once

#include <string>
#include <vector>
#include <sstream>
class Summary
{
private:
    std::string teamA;
    std::string teamB;
    bool active;
    bool beforeHalf;
    int teamAScore;
    int teamBScore;
    int finalpoessionA;
    int finalpoessionB;
    std::string gameEventReports;
public:
    Summary();
    void updateSummary(const std::string& report);
    std::string toString() const;
    std::vector<std::string> splitStringByString(const std::string& input , const std::string& delimiter);
};
