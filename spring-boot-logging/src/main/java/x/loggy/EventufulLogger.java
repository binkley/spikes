package x.loggy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.relational.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.relational.core.mapping.event.AfterLoadEvent;
import org.springframework.data.relational.core.mapping.event.AfterSaveEvent;
import org.springframework.data.relational.core.mapping.event.BeforeDeleteEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.relational.core.mapping.event.RelationalEventWithEntity;
import org.springframework.data.relational.core.mapping.event.RelationalEventWithId;
import org.springframework.data.relational.core.mapping.event.RelationalEventWithIdAndEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventufulLogger {
    private final Logger logger;

    @EventListener
    public void on(final AfterLoadEvent event) {
        onWithIdAndEntity(event);
    }

    @EventListener
    public void on(final BeforeSaveEvent event) {
        onWithEntity(event);
    }

    @EventListener
    public void on(final AfterSaveEvent event) {
        onWithIdAndEntity(event);
    }

    @EventListener
    public void on(final BeforeDeleteEvent event) {
        onWithId(event);
    }

    @EventListener
    public void on(final AfterDeleteEvent event) {
        onWithId(event);
    }

    private void onWithIdAndEntity(
            final RelationalEventWithIdAndEntity event) {
        final var source = event.getSource();
        final var timestamp = event.getTimestamp();
        final var entity = event.getEntity();
        final var optionalEntity = event.getOptionalEntity();
        final var id = event.getId();

        final var change = event.getChange();

        if (null == change) { // AfterLoadEvent
            logger.info("{}:"
                            + "\n - source: {}"
                            + "\n - timestamp: {}"
                            + "\n - entity: {}"
                            + "\n - optional-entity: {}"
                            + "\n - id: {}"
                            + "\n - change: NONE",
                    event, source, entity, timestamp, optionalEntity, id);
            return;
        }

        final var changeKind = change.getKind();
        final var changeEntityType = change.getEntityType();
        final var changeEntity = change.getEntity();
        final var changeActions = change.getActions();

        logger.info("{}:"
                        + "\n - source: {}"
                        + "\n - timestamp: {}"
                        + "\n - entity: {}"
                        + "\n - optional-entity: {}"
                        + "\n - id: {}"
                        + "\n - change: {}"
                        + "\n - change-kind: {}"
                        + "\n - change-entity-type: {}"
                        + "\n - change-entity: {}"
                        + "\n - change-actions: {}",
                event, source, timestamp, entity, optionalEntity, id, change,
                changeKind, changeEntityType, changeEntity, changeActions);
    }

    private void onWithId(final RelationalEventWithId event) {
        final var source = event.getSource();
        final var timestamp = event.getTimestamp();
        final var optionalEntity = event.getOptionalEntity();
        final var id = event.getId();

        final var change = event.getChange();

        final var changeKind = change.getKind();
        final var changeEntityType = change.getEntityType();
        final var changeEntity = change.getEntity();
        final var changeActions = change.getActions();

        logger.info("{}:"
                        + "\n - source: {}"
                        + "\n - timestamp: {}"
                        + "\n - optional-entity: {}"
                        + "\n - id: {}"
                        + "\n - change: {}"
                        + "\n - change-kind: {}"
                        + "\n - change-entity-type: {}"
                        + "\n - change-entity: {}"
                        + "\n - change-actions: {}",
                event, source, timestamp, optionalEntity, id, change,
                changeKind, changeEntityType, changeEntity, changeActions);
    }

    private void onWithEntity(final RelationalEventWithEntity event) {
        final var source = event.getSource();
        final var timestamp = event.getTimestamp();
        final var entity = event.getEntity();
        final var optionalEntity = event.getOptionalEntity();
        final var id = event.getId();

        final var change = event.getChange();

        final var changeKind = change.getKind();
        final var changeEntityType = change.getEntityType();
        final var changeEntity = change.getEntity();
        final var changeActions = change.getActions();

        logger.info("{}:"
                        + "\n - source: {}"
                        + "\n - timestamp: {}"
                        + "\n - entity: {}"
                        + "\n - optional-entity: {}"
                        + "\n - id: {}"
                        + "\n - change: {}"
                        + "\n - change-kind: {}"
                        + "\n - change-entity-type: {}"
                        + "\n - change-entity: {}"
                        + "\n - change-actions: {}",
                event, source, timestamp, entity, optionalEntity, id, change,
                changeKind, changeEntityType, changeEntity, changeActions);
    }
}
