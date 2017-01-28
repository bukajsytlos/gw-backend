package com.faforever.gw.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="gw_battle")
@NoArgsConstructor
public class Battle implements Serializable {

    public Battle(Planet planet, Faction attackingFaction, Faction defendingFaction) {
        this.planet = planet;
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.status = BattleStatus.INITIATED;
        this.initiatedAt = Timestamp.from(Instant.now());
    }

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name="fk_planet")
    private Planet planet;

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<BattleParticipant> participants = new ArrayList<>();

    @Column(name="status", nullable = false, length = 1)
    private BattleStatus status;

    @Column(name="initiated_at", nullable = false)
    private Timestamp initiatedAt;

    @Column(name="started_at")
    private Timestamp startedAt;

    @Column(name="ended_at")
    private Timestamp endedAt;

    @Column(name="attacking_faction", nullable = false, updatable = false, length = 1)
    private Faction attackingFaction;

    @Column(name="defending_faction", nullable = false, updatable = false, length = 1)
    private Faction defendingFaction;

    @Column(name="winning_faction", length = 1)
    private Faction winningFaction;
}
