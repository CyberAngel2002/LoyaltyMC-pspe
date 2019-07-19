package protocolsupport.protocol.typeremapper.pe;

import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.utils.types.Position;
import protocolsupportbuildprocessor.Preload;

@Preload
public class PELevelEvent {

	public static final int SOUND_CLICK = 1000;
	public static final int SOUND_CLICK_FAIL = 1001;
	public static final int SOUND_SHOOT = 1002;
	public static final int SOUND_DOOR = 1003;
	public static final int SOUND_FIZZ = 1004;
	public static final int SOUND_TNT = 1005;

	public static final int SOUND_GHAST = 1007;
	public static final int SOUND_BLAZE_SHOOT = 1008;
	public static final int SOUND_GHAST_SHOOT = 1009;
	public static final int SOUND_DOOR_BUMP = 1010;
	public static final int SOUND_DOOR_CRASH = 1012;

	public static final int SOUND_ENDERMAN_TELEPORT = 1018;

	public static final int SOUND_ANVIL_BREAK = 1020;
	public static final int SOUND_ANVIL_USE = 1021;
	public static final int SOUND_ANVIL_FALL = 1022;

	public static final int SOUND_ITEM_DROP = 1030;
	public static final int SOUND_ITEM_THROWN = 1031;

	public static final int SOUND_PORTAL = 1032;

	public static final int SOUND_ITEM_FRAME_ITEM_ADDED = 1040;
	public static final int SOUND_ITEM_FRAME_PLACED = 1041;
	public static final int SOUND_ITEM_FRAME_REMOVED = 1042;
	public static final int SOUND_ITEM_FRAME_ITEM_REMOVED = 1043;
	public static final int SOUND_ITEM_FRAME_ITEM_ROTATED = 1044;

	public static final int SOUND_CAMERA_TAKE_PICTURE = 1050;
	public static final int SOUND_EXPERIENCE_ORB = 1051;
	public static final int SOUND_BLOCK_PLACE = 1052;

	public static final int GUARDIAN_CURSE = 2006;

	public static final int SOUND_BUTTON_CLICK = 3500;
	public static final int SOUND_EXPLODE = 3501;
	public static final int CAULDRON_DYE_ARMOR = 3502;
	public static final int CAULDRON_CLEAN_ARMOR = 3503;
	public static final int CAULDRON_FILL_POTION = 3504;
	public static final int CAULDRON_TAKE_POTION = 3505;
	public static final int SOUND_SPLASH = 3506;
	public static final int CAULDRON_TAKE_WATER = 3507;
	public static final int CAULDRON_ADD_DYE = 3508;

	public static final int START_RAIN = 3001;
	public static final int START_THUNDER = 3002;
	public static final int STOP_RAIN = 3003;
	public static final int STOP_THUNDER = 3004;

	public static final int SOUND_CAULDRON = 3501;
	public static final int SOUND_CAULDRON_DYE_ARMOR = 3502;
	public static final int SOUND_CAULDRON_FILL_POTION = 3504;
	public static final int SOUND_CAULDRON_FILL_WATER = 3506;

	public static final int BLOCK_START_BREAK = 3600;
	public static final int BLOCK_STOP_BREAK = 3601;

	public static final int SET_DATA = 4000;

	public static final int PLAYERS_SLEEPING = 9800;
	
    // 2 same as 1
    public static final int TYPE_CRITICAL = 3;
    public static final int TYPE_BLOCK_FORCE_FIELD = 4;
    public static final int TYPE_SMOKE = 5;
    public static final int TYPE_EXPLODE = 6;
    public static final int TYPE_EVAPORATION = 7;
    public static final int TYPE_FLAME = 8;
    public static final int TYPE_LAVA = 9;
    public static final int TYPE_LARGE_SMOKE = 10;
    public static final int TYPE_REDSTONE = 11;
    public static final int TYPE_RISING_RED_DUST = 12;
    // 62 same as 12
    public static final int TYPE_ITEM_BREAK = 13;
    public static final int TYPE_SNOWBALL_POOF = 14;
    public static final int TYPE_HUGE_EXPLODE = 15;
    // 60 same as 15
    public static final int TYPE_HUGE_EXPLODE_SEED = 16;
    public static final int TYPE_MOB_FLAME = 17;
    public static final int TYPE_HEART = 18;
    public static final int TYPE_TERRAIN = 19;
    public static final int TYPE_SUSPENDED_TOWN = 20, TYPE_TOWN_AURA = 20;
    // 61 same as 20
    public static final int TYPE_PORTAL = 21;
    // 22 same as 21
    public static final int TYPE_SPLASH = 23, TYPE_WATER_SPLASH = 23;
    // 24 same as 23
    public static final int TYPE_WATER_WAKE = 25;
    public static final int TYPE_DRIP_WATER = 26;
    public static final int TYPE_DRIP_LAVA = 27;
    public static final int TYPE_FALLING_DUST = 28, TYPE_DUST = 28;
    public static final int TYPE_MOB_SPELL = 29;
    public static final int TYPE_MOB_SPELL_AMBIENT = 30;
    public static final int TYPE_MOB_SPELL_INSTANTANEOUS = 31;
    public static final int TYPE_INK = 32;
    public static final int TYPE_SLIME = 33;
    public static final int TYPE_RAIN_SPLASH = 34;
    public static final int TYPE_VILLAGER_ANGRY = 35;
    // 59 same as 35
    public static final int TYPE_VILLAGER_HAPPY = 36;
    public static final int TYPE_ENCHANTMENT_TABLE = 37;
    public static final int TYPE_TRACKING_EMITTER = 38;
    public static final int TYPE_NOTE = 39;
    public static final int TYPE_WITCH_SPELL = 40;
    public static final int TYPE_CARROT = 41;
    // 42 unknown
    public static final int TYPE_END_ROD = 43;
    // 58 same as 43
    public static final int TYPE_DRAGONS_BREATH = 44;
    public static final int TYPE_SPIT = 45;
    public static final int TYPE_TOTEM = 46;
    public static final int TYPE_FOOD = 47;
    public static final int TYPE_FIREWORKS_STARTER = 48;
    public static final int TYPE_FIREWORKS_SPARK = 49;
    public static final int TYPE_FIREWORKS_OVERLAY = 50;
    public static final int TYPE_BALLOON_GAS = 51;
    public static final int TYPE_COLORED_FLAME = 52;
    public static final int TYPE_SPARKLER = 53;
    public static final int TYPE_CONDUIT = 54;
    public static final int TYPE_BUBBLE_COLUMN_UP = 55;
    public static final int TYPE_BUBBLE_COLUMN_DOWN = 56;
    public static final int TYPE_SNEEZE = 57;

	public static final int TYPE = 0x4000,
		TYPE_BUBBLE 					= TYPE |  1,
		TYPE_CRITICAL 					= TYPE |  2,
		TYPE_BLOCK_FORCE_FIELD 			= TYPE |  3,
		TYPE_SMOKE 						= TYPE |  4,
		TYPE_EXPLODE 					= TYPE |  5,
		TYPE_EVAPORTAION 				= TYPE |  6,
		TYPE_FLAME 						= TYPE |  7,
		TYPE_LAVA 						= TYPE |  8,
		TYPE_LARGE_SMOKE 				= TYPE |  9,
		TYPE_REDSTONE 					= TYPE | 10,
		TYPE_RISING_RED_DUST 			= TYPE | 11,
		TYPE_ITEM_BREAK 				= TYPE | 12,
		TYPE_SNOWBALL_POOF 				= TYPE | 13,
		TYPE_HUGE_EXPLOSION				= TYPE | 14,
		TYPE_HUGE_EXPLOSION_SEED		= TYPE | 15,
		TYPE_MOB_FLAME 					= TYPE | 16,
		TYPE_HEART 						= TYPE | 17,
		TYPE_TERRAIN 					= TYPE | 18,
		TYPE_TOWN_AURA		 			= TYPE | 19,
		TYPE_PORTAL 					= TYPE | 20,
		TYPE_SPLASH 					= TYPE | 21,
		TYPE_WATER_WAKE 				= TYPE | 22,
		TYPE_DRIP_WATER 				= TYPE | 23,
		TYPE_DRIP_LAVA 					= TYPE | 24,
		TYPE_FALLING_DUST 				= TYPE | 25,
		TYPE_MOB_SPELL 					= TYPE | 26,
		TYPE_MOB_SPELL_AMBIENT 			= TYPE | 27,
		TYPE_MOB_SPELL_INSTANTANIOUS	= TYPE | 28,
		TYPE_INK 						= TYPE | 29,
		TYPE_SLIME 						= TYPE | 30,
		TYPE_RAIN_SPLASH 				= TYPE | 31,
		TYPE_VILLAGER_ANGRY 			= TYPE | 32,
		TYPE_VILLAGER_HAPPY 			= TYPE | 33,
		TYPE_ENCHANTMENT_TABLE 			= TYPE | 34,
		TYPE_TRACKING_EMITTER 			= TYPE | 35,
		TYPE_NOTE 						= TYPE | 36,
		TYPE_WITCH_SPELL 				= TYPE | 37,
		TYPE_CARROT 					= TYPE | 38,
		TYPE_UNKNOWN_1 					= TYPE | 39,
		TYPE_END_ROT 					= TYPE | 40,
		TYPE_DRAGONS_BREATH 			= TYPE | 41;

	public static ClientBoundPacketData createPacket(int levelEvent, float x, float y, float z, int data) {
		ClientBoundPacketData clientLevelEvent = ClientBoundPacketData.create(PEPacketIDs.LEVEL_EVENT);
		VarNumberSerializer.writeSVarInt(clientLevelEvent, levelEvent);
		clientLevelEvent.writeFloatLE(x);
		clientLevelEvent.writeFloatLE(y);
		clientLevelEvent.writeFloatLE(z);
		VarNumberSerializer.writeSVarInt(clientLevelEvent, data);
		return clientLevelEvent;
	}

	public static ClientBoundPacketData createPacket(int levelEvent, Position position, int data) {
		return createPacket(levelEvent, position.getX(), position.getY(), position.getZ(), data);
	}

	public static ClientBoundPacketData createPacket(int levelEvent, Position position) {
		return createPacket(levelEvent, position, 0);
	}

	public static ClientBoundPacketData createPacket(int levelEvent, int data) {
		return createPacket(levelEvent, 0f, 0f, 0f, data);
	}

	public static ClientBoundPacketData createPacket(int levelEvent) {
		return createPacket(levelEvent, 0);
	}

}
