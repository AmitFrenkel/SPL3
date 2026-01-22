#include "../include/StompProtocol.h"

StompProtocol::StompProtocol(ConnectionHandler*& handler) : handler(handler), subscribeId(0), 
receiptId(0), channelToId(), pendingReceipts()  {}

bool StompProtocol::connect(std::string host, std::string user, std::string pass) {
    std::string frame = "CONNECT\naccept-version:1.2\nhost:" + host + "\nlogin:" + user + "\npasscode:" + pass + "\n\n";
    return handler->sendFrameAscii(frame, '\0');
}

void StompProtocol::subscribe(std::string channel) {
    channelToId[channel] = subscribeId;
    pendingReceipts[receiptId] = "Joined channel " + channel;
    std::string frame = "SUBSCRIBE\ndestination:/" + channel + "\nid:" + std::to_string(subscribeId)
     + "\nreceipt:" + std::to_string(receiptId) + "\n\n";
    handler->sendFrameAscii(frame, '\0');
    subscribeId++;
    receiptId++;
}

void StompProtocol::unsubscribe(std::string channel) {
    int subId = channelToId[channel];
    pendingReceipts[receiptId] = "Exited channel " + channel;
    std::string frame = "UNSUBSCRIBE\nid:" + std::to_string(subId) + "\nreceipt:" + std::to_string(receiptId) + "\n\n";
    handler->sendFrameAscii(frame, '\0');
    receiptId++;
}

void StompProtocol::sendMessage(std::string msg) {
    handler->sendFrameAscii(msg, '\0');
}

void StompProtocol::disconnect() {
    pendingReceipts[receiptId] = "";
    std::string frame = "DISCONNECT\nreceipt:" + std::to_string(receiptId) + "\n\n";
    handler->sendFrameAscii(frame, '\0');
    receiptId++;
}

void StompProtocol::handleReceipt(std::string receipt_frame) {
        std::string receipt_id_str = "receipt-id:";
        size_t pos = receipt_frame.find(receipt_id_str);
        if (pos != std::string::npos) {
            size_t start = pos + receipt_id_str.length();
            size_t end = receipt_frame.find('\n', start);
            std::string receipt_id_substr = receipt_frame.substr(start, end - start);
            int receipt_id = std::stoi(receipt_id_substr);
            auto it = pendingReceipts.find(receipt_id);
            if (it != pendingReceipts.end()) {
                if (it->second == "")
                {
                    std::cout << receipt_frame << std::endl;
                }
                else{
                    std::cout << it->second << std::endl;
                }
                pendingReceipts.erase(it);
            }
        }
    }
 