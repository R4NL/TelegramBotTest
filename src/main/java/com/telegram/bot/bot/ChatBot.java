package com.telegram.bot.bot;

import com.telegram.bot.entity.User;
import com.telegram.bot.jpaRepository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ChatBot extends TelegramLongPollingBot {
    @Autowired
    private UserJpaRepository userJpaRepository;

    private final String botName = "TestBotPoweredByTheAlEShKA_bot";
    private final String botToken = "1024273765:AAHPt9_EAB4Ylzre-I6DxBx8dk4XUp7lAns";

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
