package alle.dupuch.tp1.message;

import alle.dupuch.tp1.Agent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue {

    private static HashMap<Integer, Queue<Message>> reqList;

    public static void init(List<Agent> agents) {
        reqList = new HashMap<>();
        for (Agent agent: agents){
            reqList.put(agent.getId(), new ConcurrentLinkedQueue<>());
        }
    }

    public static void add(Message message) {
        putIfNotExists(message.getReceiver(), message.getType());
        if(message.getType() == MessageType.REQUEST_MOVE) {
            if(!reqList.get(message.getReceiver()).contains(message))
                reqList.get(message.getReceiver()).add(message);
        }
    }

    public static Message getNext(Agent agent, MessageType type) {
        putIfNotExists(agent.getId(), type);
        if(type == MessageType.REQUEST_MOVE) {
            return reqList.get(agent.getId()).poll();
        }
        return null;
    }

    private static void putIfNotExists(int id, MessageType type) {
        if(type == MessageType.REQUEST_MOVE) {
            if (!reqList.containsKey(id)) {
                reqList.put(id, new ConcurrentLinkedQueue<>());
            }
        }
    }
}