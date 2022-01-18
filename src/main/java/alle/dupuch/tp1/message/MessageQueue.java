package alle.dupuch.tp1.message;

import alle.dupuch.tp1.Agent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue {

    private static HashMap<Integer, Queue<Message>> reqList;
    private static HashMap<Integer, Queue<Message>> respList;

    public static void init(List<Agent> agents) {
        reqList = new HashMap<>();
        respList = new HashMap<>();
        for (Agent agent: agents){
            reqList.put(agent.getId(), new ConcurrentLinkedQueue<>());
            respList.put(agent.getId(), new ConcurrentLinkedQueue<>());
        }
    }

    public static void add(Message message) {
        putIfNotExists(message.getReceiver(), message.getType());
        if(message.getType() == MessageType.REQUEST_MOVE) {
            if(!reqList.get(message.getReceiver()).contains(message))
                reqList.get(message.getReceiver()).add(message);
        }
        if(message.getType() == MessageType.RESPONSE_MOVE) {
            if(!reqList.get(message.getReceiver()).contains(message))
                respList.get(message.getReceiver()).add(message);
        }
    }

    public static Message getNext(Agent agent, MessageType type) {
        putIfNotExists(agent.getId(), type);
        if(type == MessageType.REQUEST_MOVE) {
            return reqList.get(agent.getId()).poll();
        }
        if(type == MessageType.RESPONSE_MOVE) {
            return respList.get(agent.getId()).poll();
        }
        return null;
    }

    private static void putIfNotExists(int id, MessageType type) {
        if(type == MessageType.REQUEST_MOVE) {
            if (!reqList.containsKey(id)) {
                reqList.put(id, new ConcurrentLinkedQueue<>());
            }
        }
        if(type == MessageType.RESPONSE_MOVE) {
            if (!respList.containsKey(id)) {
                respList.put(id, new ConcurrentLinkedQueue<>());
            }
        }
    }
}