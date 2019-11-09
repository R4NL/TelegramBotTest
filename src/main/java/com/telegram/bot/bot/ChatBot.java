package com.telegram.bot.bot;

import com.telegram.bot.entity.User;
import com.telegram.bot.jpaRepository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Component
public class ChatBot extends TelegramLongPollingBot {
    @Autowired
    private UserJpaRepository userJpaRepository;

    private final String botName;
    private final String botToken;

    {
        String botNameInitializer = new String();
        String botTokenInitializer = new String();

        try {
            botNameInitializer = Files.readAllLines(Path.of("config/botToken.txt")).stream().filter(n -> n.contains("name"))
                    .map(s -> s.substring(s.indexOf("=") + 1)).collect(Collectors.joining());
            botTokenInitializer = Files.readAllLines(Path.of("config/botToken.txt")).stream().filter(n -> n.contains("token"))
                    .map(s -> s.substring(s.indexOf("=") + 1)).collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        botName = botNameInitializer;
        botToken = botTokenInitializer;
    }

    private BotState state;
    private BotContext context;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() && !update.getMessage().hasText()) {
            return;
        }

        User user = userJpaRepository.findByChatId(update.getMessage().getChatId());

        if (user == null) {
            state = BotState.getInstance();
            user = userJpaRepository.save(new User().setChatId(update.getMessage().getChatId())
                    .setStateId(state.ordinal()));
            context = BotContext.of(this, user, update.getMessage().getText());
            state.enter(context);
        } else {
            context = BotContext.of(this, user, update.getMessage().getText());
            state = BotState.byId(user.getStateId());
        }
        state.handleInput(context);
        do {
            state = state.nextState();
            System.out.println(context);
            state.enter(context);

        } while (!state.isInputNeeded());
        user.setStateId(state.ordinal());
        user.setGroupInUni(context.getUser().getGroupInUni());
        userJpaRepository.save(user);
    }


    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
