package com.telegram.bot.bot;

import com.telegram.bot.servise.WeekDay;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum BotState {
    Start {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Hello!\n Enter your group, use /group");
        }

        @Override
        public BotState nextState() {
            return Wait;
        }
    },

    EnterGroup {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your group, please:");
        }

        @Override
        public void handleInput(BotContext context) {
            if (checkGroupName(context.getInput())) {
                context.getUser().setGroupInUni(context.getInput());
                next = EndInputGroup;
            } else {
                next = EnterGroup;
                sendMessage(context, "you enter wrong group name");
            }


        }

        private boolean checkGroupName(String groupName) {
            try {
                return Files.walk(Path.of("data/")).filter(Files::isDirectory).map(Path::getFileName).map(Path::toString)
                        .filter(n -> !n.equals("data")).map(n -> n.equalsIgnoreCase(groupName))
                        .reduce((aBoolean, aBoolean2) -> aBoolean || aBoolean2).get();
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    EndInputGroup(false) {
        @Override
        public void enter(BotContext context) {
            try {
                sendMessage(context, Files.readString(Path.of("config/instruction.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public BotState nextState() {
            return Wait;
        }
    },

    Wait {
        private BotState next;

        @Override
        public void enter(BotContext context) {
        }

        @Override
        public void handleInput(BotContext context) {
            next = Wait;
            switch (context.getInput()) {
                case "/today":
                    today(context);
                    break;
                case "/tomorrow":
                    tomorrow(context);
                    break;
                case "/week":
                    week(context);
                    break;
                case "/group":
                    next = EnterGroup;
                    break;
                default:
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }

        private void today(BotContext context) {
            String url = "data/" + context.getUser().getGroupInUni() +
                    "/" + WeekDay.getWeekNumToPackage() + "/" + WeekDay.today() + ".txt";
            try {
                System.out.println(url);
                sendMessage(context, Files.readString(Path.of(url)));
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage(context, "no classes found");
            }
        }

        private void tomorrow(BotContext context) {
            String url = "data/" + context.getUser().getGroupInUni() + "/" + WeekDay.getWeekNumToPackage() + "/" + WeekDay.tomorrow() + ".txt";
            try {
                sendMessage(context, Files.readString(Path.of(url)));
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                sendMessage(context, "no classes found");
            }
        }

        private void week(BotContext context) {
            String url = "data/" + context.getUser().getGroupInUni() + "/" + WeekDay.getWeekNumToPackage();
            try {
                Map<String, List<Path>> fileNameMap = Files.walk(Path.of(url)).filter(Files::isRegularFile).collect(Collectors.groupingBy(n -> n.getFileName()
                        .toString().replace(".txt", "")));

                String result = Arrays.stream(WeekDay.values()).map(n -> {
                    try {
                        return n + "\n" + Files.readString(fileNameMap.get(n + "").get(0)) + "\n\n  ";
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                        return "";
                    }
                }).filter(n -> (!n.equals("") && n != null)).reduce((s, s1) -> s + s1).get();
                sendMessage(context, result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private static List<BotState> states;
    private final boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }

    BotState(boolean inputNeeded) {
        this.inputNeeded = inputNeeded;
    }

    public static BotState getInstance() {
        return byId(0);
    }

    public static BotState byId(int i) {
        if (states == null) {
            states = Arrays.asList(BotState.values());
        }
        if (i >= states.size()) {
            throw new IllegalArgumentException("No such state.");
        }
        return states.get(i);
    }

    void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(context.getUser().getChatId())
                .setText(text);
        try {
            context.getBot().execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    public void handleInput(BotContext context) {

    }

    public abstract void enter(BotContext context);

    public abstract BotState nextState();
}
