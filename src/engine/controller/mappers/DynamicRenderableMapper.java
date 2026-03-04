package engine.controller.mappers;

import java.util.ArrayList;

import engine.model.bodies.ports.BodyData;
import engine.model.bodies.ports.BodyType;
import engine.model.physics.ports.PhysicsValuesDTO;
import engine.utils.pooling.PoolMDTO;
import engine.view.renderables.ports.DynamicRenderDTO;

public class DynamicRenderableMapper extends DTOPooledMapper<DynamicRenderDTO> {

    private static final double PLAYER_SPRITE_ANGLE_OFFSET_DEG = 90.0d;

    public static DynamicRenderDTO fromBodyDTO(BodyData bodyData) {
        PhysicsValuesDTO phyValues = bodyData.getPhysicsValues();

        if (phyValues == null || bodyData.entityId == null) {
            return null;
        }

        DynamicRenderDTO renderablesData = new DynamicRenderDTO(
                bodyData.entityId,
                phyValues.posX, phyValues.posY,
            toRenderAngle(bodyData.type, phyValues.angle),
                phyValues.size,
                phyValues.timeStamp,
                phyValues.speedX, phyValues.speedY,
                phyValues.accX, phyValues.accY,
                phyValues.timeStamp);

        return renderablesData;
    }

    public static ArrayList<DynamicRenderDTO> fromBodyDTO(ArrayList<BodyData> bodyData) {
        ArrayList<DynamicRenderDTO> renderableValues = new ArrayList<>();

        for (BodyData bodyDto : bodyData) {
            DynamicRenderDTO renderable = DynamicRenderableMapper.fromBodyDTO(bodyDto);
            renderableValues.add(renderable);
        }

        return renderableValues;
    }

    // region Pooled mapper

    public DynamicRenderableMapper(PoolMDTO<DynamicRenderDTO> pool) {
        super(pool);
    }

    public DynamicRenderDTO fromBodyDTOPooled(BodyData bodyData) {
        return this.map(bodyData);
    }

    public ArrayList<DynamicRenderDTO> fromBodyDTOPooled(ArrayList<BodyData> bodyDataList) {
        ArrayList<DynamicRenderDTO> renderables = new ArrayList<>();

        for (BodyData bodyData : bodyDataList) {
            DynamicRenderDTO dto = this.fromBodyDTOPooled(bodyData);
            if (dto != null) {
                renderables.add(dto);
            }
        }

        return renderables;
    }

    @Override
    protected boolean mapToDTO(Object source, DynamicRenderDTO target) {
        if (!(source instanceof BodyData)) {
            return false;
        }

        BodyData bodyData = (BodyData) source;
        PhysicsValuesDTO phyValues = bodyData.getPhysicsValues();

        if (phyValues == null || bodyData.entityId == null) {
            return false;
        }

        target.updateFrom(
                bodyData.entityId,
                phyValues.posX, phyValues.posY,
            toRenderAngle(bodyData.type, phyValues.angle),
                phyValues.size,
                phyValues.timeStamp,
                phyValues.speedX, phyValues.speedY,
                phyValues.accX, phyValues.accY,
                phyValues.timeStamp);

        return true;
    }

    // endregion

    private static double toRenderAngle(BodyType bodyType, double physicalAngle) {
        if (bodyType != BodyType.PLAYER) {
            return physicalAngle;
        }

        double angle = physicalAngle + PLAYER_SPRITE_ANGLE_OFFSET_DEG;
        angle %= 360.0d;
        if (angle < 0.0d) {
            angle += 360.0d;
        }
        return angle;
    }
}