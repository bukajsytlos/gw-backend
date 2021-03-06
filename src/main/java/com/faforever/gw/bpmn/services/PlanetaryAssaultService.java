package com.faforever.gw.bpmn.services;

import com.faforever.gw.bpmn.message.generic.UserErrorMessage;
import com.faforever.gw.model.*;
import com.faforever.gw.model.repository.PlanetRepository;
import com.faforever.gw.security.User;
import com.faforever.gw.services.messaging.MessagingService;
import com.faforever.gw.websocket.incoming.InitiateAssaultMessage;
import com.faforever.gw.websocket.incoming.JoinAssaultMessage;
import com.faforever.gw.websocket.incoming.LeaveAssaultMessage;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
/**
 * Service class for the BPMN process "planetary assault"
 */
public class PlanetaryAssaultService {
    public static final String UPDATE_OPEN_GAMES_SIGNAL = "Signal_UpdateOpenGames";
    public static final String INITIATE_ASSAULT_MESSAGE = "Message_InitiateAssault";
    public static final String PLAYER_JOINS_ASSAULT_MESSAGE = "Message_PlayerJoinsAssault";
    public static final String PLAYER_LEAVES_ASSAULT_MESSAGE = "Message_PlayerLeavesAssault";
    public static final String GAME_RESULT_MESSAGE = "Message_GameResult";
    public static final Long XP_MALUS_FOR_RECALL = 5L;
    private final ApplicationContext applicationContext;
    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    private final MessagingService messagingService;
    private final PlanetRepository planetRepository;

    @Inject
    public PlanetaryAssaultService(ApplicationContext applicationContext, ProcessEngine processEngine, RuntimeService runtimeService, MessagingService messagingService, PlanetRepository planetRepository) {
        this.applicationContext = applicationContext;
        this.processEngine = processEngine;
        this.runtimeService = runtimeService;
        this.messagingService = messagingService;
        this.planetRepository = planetRepository;
    }

    @Transactional(dontRollbackOn = BpmnError.class)
    public void onCharacterInitiatesAssault(InitiateAssaultMessage message, User user) {
        log.debug("onCharacterInitiatesAssault by user {}", user.getId());
        UUID battleUUID = UUID.randomUUID();

        GwCharacter character = user.getActiveCharacter();
        Planet planet = planetRepository.findOne(message.getPlanetId());

        VariableMap variables = messagingService.createVariables(message.getRequestId(), user.getActiveCharacter().getId())
                .putValue("battle", battleUUID)
                .putValue("planet", planet.getId())
                .putValue("attackingFaction", character.getFaction())
                .putValue("defendingFaction", planet.getCurrentOwner())
                .putValue("attackerCount", 1)
                .putValue("defenderCount", 0)
                .putValue("gameFull", false)
                .putValue("waitingProgress", 0.0d)
                .putValue("winner", "t.b.d.");

        log.debug("-> added processVariables: {}", variables);
        runtimeService.startProcessInstanceByMessage(INITIATE_ASSAULT_MESSAGE, battleUUID.toString(), variables);
    }

    public void onCharacterJoinsAssault(JoinAssaultMessage message, User user) {
        log.debug("onCharacterJoinsAssault for battle {}", message.getBattleId());

        VariableMap variables = messagingService.createVariables(message.getRequestId(), user.getActiveCharacter().getId());

        try {
            runtimeService.correlateMessage(PLAYER_JOINS_ASSAULT_MESSAGE, message.getBattleId().toString(), variables);
        } catch (MismatchingMessageCorrelationException e) {
            log.error("Battle {} is no active bpmn instance", message.getBattleId());
            sendErrorToUser(user, message.getRequestId(), GwErrorType.BATTLE_INVALID);
        }
    }

    public void onCharacterLeavesAssault(LeaveAssaultMessage message, User user) {
        log.debug("onCharacterLeavesAssault for battle {}", message.getBattleId().toString());

        VariableMap variables = messagingService.createVariables(message.getRequestId(), user.getActiveCharacter().getId());

        try {
            runtimeService.correlateMessage(PLAYER_LEAVES_ASSAULT_MESSAGE, message.getBattleId().toString(), variables);
        } catch (MismatchingMessageCorrelationException e) {
            log.error("Battle {} is no active bpmn instance", message.getBattleId());
            sendErrorToUser(user, message.getRequestId(), GwErrorType.BATTLE_INVALID);
        }
    }

    private void sendErrorToUser(User user, UUID requestId, GwErrorType errorType) {
        UserErrorMessage errorMessage = applicationContext.getBean(UserErrorMessage.class);
        errorMessage.setRequestId(requestId);
        errorMessage.setRecepientCharacter(user.getActiveCharacter().getId());
        errorMessage.setErrorCode(errorType.getErrorCode());
        errorMessage.setErrorMessage(errorType.getErrorMessage());
        messagingService.send(errorMessage);
    }

    @Scheduled(fixedDelay = 60000)
    public void updateOpenGames() {
        runtimeService.signalEventReceived(UPDATE_OPEN_GAMES_SIGNAL);
    }

    @Transactional(dontRollbackOn = BpmnError.class)
    public void onGameResult(GameResult gameResult) {
        UUID battleId = gameResult.getBattle();

        VariableMap variables = Variables.createVariables()
                .putValue("gameResult", gameResult);

        runtimeService.correlateMessage(GAME_RESULT_MESSAGE, battleId.toString(), variables);
    }

    public Long calcFactionVictoryXpForCharacter(Battle battle, GwCharacter character) {
        Optional<BattleParticipant> participantOptional = battle.getParticipant(character);

        if (participantOptional.isPresent()) {
            BattleParticipant participant = participantOptional.get();

            Long noOfAllies = battle.getParticipants().stream()
                    .filter(battleParticipant -> battleParticipant.getFaction() == character.getFaction())
                    .count();

            Long noOfEnemies = battle.getParticipants().stream()
                    .filter(battleParticipant -> battleParticipant.getFaction() != character.getFaction())
                    .count();

            if (battle.getWinningFaction() == character.getFaction()) {
                Long gainedXP = Math.round(10.0 * noOfEnemies / Math.pow( noOfAllies * 0.9, noOfAllies - 1 ));

                if(participant.getResult() == BattleParticipantResult.RECALL) {
                    gainedXP -= XP_MALUS_FOR_RECALL;
                }

                return gainedXP;
            } else {
                return 0L;
            }
        } else {
            throw new RuntimeException(String.format("Character {} didn't participate in battle {}", character.getId(), battle.getId()));
        }
    }

    public Long calcTeamkillXpMalus(GwCharacter character) {
        return Math.round(30.0 / Math.pow(0.88, character.getRank().getLevel()));
    }

    public Long calcKillXpBonus(GwCharacter killer, GwCharacter victim) {
        // Killing an ACU is worth 5 points per Rank, with an added factor for drop off with higher ranked players
        // + 5% bonus per rank the victim was higher than the killer

        Integer killerRank = killer.getRank().getLevel();
        Double rankDifferenceFactor = Math.min(0.0,victim.getRank().getLevel() - killerRank) * 5 / 100.0;
        return Math.round(5*killerRank*Math.pow(0.99, killerRank-1)*rankDifferenceFactor);
    }

    public double calcWaitingProgress(int mapSlots, long attackerCount, long defenderCount) {
        VariableMap attackerVariables = Variables.createVariables()
                .putValue("map_slots", mapSlots)
                .putValue("faction_player_count", attackerCount);

        VariableMap defenderVariables = Variables.createVariables()
                .putValue("map_slots", mapSlots)
                .putValue("faction_player_count", defenderCount);

        DecisionService decisionService = processEngine.getDecisionService();
        DmnDecisionTableResult attackerResult = decisionService.evaluateDecisionTableByKey("assault_progress_factor", attackerVariables);
        DmnDecisionTableResult defenderResult = decisionService.evaluateDecisionTableByKey("assault_progress_factor", defenderVariables);

        // we can securely access getFirstResult, because the DMN table gives a unique result
        Double attackerProgress = attackerResult.getFirstResult().getEntry("progress_factor");
        Double defenderProgress = defenderResult.getFirstResult().getEntry("progress_factor");

        Double progressNormalizer = mapSlots * 20.0;

        return (attackerCount * attackerProgress + defenderCount * defenderProgress) / progressNormalizer;
    }
}
