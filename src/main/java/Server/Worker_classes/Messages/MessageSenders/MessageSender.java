package Server.Worker_classes.Messages.MessageSenders;

import Client.Messages.Message;
import Server.DataBaseSingleton;
import Server.Worker_classes.Admission;
import Server.Worker_classes.Messages.MessageFactory.MessageFactory;
import Server.Worker_classes.Messages.MessageFactory.SimpleBankMessageFactory;
import Server.Worker_classes.Messages.MessageFactory.StockBuySellMessageFactory;
import Server.Worker_classes.Messages.MessageFactory.TransferMessageFactory;
import Server.Worker_classes.Messages.MessageType;
import Server.Worker_classes.StockAdmission;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class MessageSender {
    MessageSender nextSender;
    Admission admission;
    StockAdmission stockAdmission;
    DataBaseSingleton dataBase;

    public MessageSender(Admission admission, StockAdmission stockAdmission, DataBaseSingleton dataBase) {
        this.admission = admission;
        this.stockAdmission = stockAdmission;
        this.dataBase = dataBase;
    }

    // TODO: Template Method
    public abstract String getQuery();

    public ArrayList<Message> getMessage(ArrayList<Message> messages, int userID){
        String query = getQuery();
        try(PreparedStatement statement = dataBase.getConnection().prepareStatement(query)){
            statement.setInt(1, userID);
            ResultSet table = statement.executeQuery();
            while (table.next()){
                putMessagesFromResultSet(messages,table);
            }
            if(nextSender == null){
                return messages;
            }
            return nextSender.getMessage(messages, userID);
        } catch (SQLException e) {
            System.out.println("Error when try to get Stock messages from user "+userID+", " + e.getMessage());
        }
        return messages;
    };

    // TODO: Factory Method (20.12.2023)
    protected void putMessagesFromResultSet(ArrayList<Message> messages, ResultSet table) throws SQLException {
        int messageType = table.getInt("message_type");
        MessageFactory factory = getFactoryByMessageType(messageType);
        if(factory == null) return;
        while (table.next()){
            messages.add(factory.getMessage(table));
        }
    }
    protected MessageFactory getFactoryByMessageType(int messageType){
        switch (messageType) {
            case MessageType.SIMPLE_MESSAGE ->{
                return new SimpleBankMessageFactory();
            }case MessageType.TRANSFER_MESSAGE -> {
                return new TransferMessageFactory();
            }case MessageType.STOCK_BUY_MESSAGE, MessageType.STOCK_SELL_MESSAGE -> {
                return new StockBuySellMessageFactory();
            }default -> {
                return null;
            }
        }
    }

    // TODO: 22.12.2023 Chain of Resp
    public void setNextSender(MessageSender nextSender){
        this.nextSender = nextSender;
    }

}

