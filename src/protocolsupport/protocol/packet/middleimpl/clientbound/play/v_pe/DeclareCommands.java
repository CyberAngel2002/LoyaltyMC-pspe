package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_pe;

import io.netty.buffer.ByteBuf;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleDeclareCommands;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.protocol.utils.ProtocolVersionsHelper;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableEmptyList;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class DeclareCommands extends MiddleDeclareCommands {

	private static final byte FLAG_IS_LITERAL = 1;
	private static final byte FLAG_IS_ARGUMENT = 2;
	private static final byte FLAG_HAS_REDIRECT = 8;
	private static final byte FLAG_HAS_SUGGESTION = 16;

	public DeclareCommands(ConnectionImpl connection) {
		super(connection);
	}

	@Override
	public void readFromServerData(ByteBuf from) {
		//super.readFromServerData(serverdata);
		int length = VarNumberSerializer.readVarInt(from);

		System.out.println("array positions: " + length);
		for (int i = 0; i < length; i++) {
			byte flags = from.readByte();
			int redirect = -1;
			int[] cmdPositions = ArraySerializer.readVarIntVarIntArray(from);
			if ((flags & FLAG_HAS_REDIRECT) != 0) {
				redirect = VarNumberSerializer.readVarInt(from);
			}
			System.out.println("cmd #" + i + " flags " + flags + " redirect: " + redirect);
			if ((flags & FLAG_IS_LITERAL) != 0) {
				String literal = StringSerializer.readString(from, ProtocolVersionsHelper.LATEST_PC);
				System.out.println("got LITERAL, name:" + literal);

			} else if ((flags & FLAG_IS_ARGUMENT) != 0) {
				String name = StringSerializer.readVarIntUTF8String(from);
				String argType = StringSerializer.readVarIntUTF8String(from);
				// now read different stuff depending on argType. *sigh*

//				ByteBuf data;
//				data = MiscSerializer.readAllBytesSlice(from);

				System.out.println("got ARG, name: " + name + ", type: " + argType );

				if (argType.equals("brigadier:string")) {
					// for string, this is most likely a varint specifying type enum:...?
					int stringType = VarNumberSerializer.readVarInt(from);
					System.out.println("BRIG-STRING: data" + stringType);
				} else if (argType.equals("minecraft:entity")) {
					byte flag = from.readByte();
					System.out.println("ENTITY: flag:" + flag);
				} else if (argType.equals("brigadier:integer")) {
					byte flag = from.readByte();
					int min = -2147483648;
					int max = 2147483647;
					if ((flag & 1) != 0) {
						// read min value
						min = from.readInt();
					}
					if ((flag & 2) != 0) {
						// read max value
						max = from.readInt();
					}

					System.out.println("INTEGER: flag:" + flag + " min" + min + " max " + max);
				} else if (argType.equals("minecraft:block_pos")) {
					// void
					System.out.println("block_pos: void:");
				} else if (argType.equals("minecraft:game_profile")) {
					// void
					System.out.println("game_profile: void:");
				} else if (argType.equals("minecraft:score_holder")) {
					// NOT DONE
					System.out.println("score_holder: BREAKING NOW");

					ByteBuf data;
					data = MiscSerializer.readAllBytesSlice(from);
					return;
				} else {
					System.out.println("UNHANDLED TYHPE: " + argType);
				}

				/* minecraft:
				// DUMP HEX
				byte[] bytes = new byte[19853];
				from.getBytes(0, bytes);
				System.out.println(bytes);
				System.out.println("as hex:");
				System.out.println(bytesToHex(bytes));
				return;
				 */

				String suggestion = "";
				if ((flags & FLAG_HAS_SUGGESTION) != 0) {
					suggestion = StringSerializer.readVarIntUTF8String(from);


				}
				System.out.println("got ARG, name: " + name + ", type: " + argType + ", suggest:" + suggestion);
			}
		}
		System.out.println("READ DECLARE FROM SERVER");
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static ClientBoundPacketData create() {
		ClientBoundPacketData serializer = ClientBoundPacketData.create(PEPacketIDs.TAB_COMPLETE);
		// Write enumValues, a way to number strings
		// size
		VarNumberSerializer.writeVarInt(serializer, 0);
		// then one string per index

		// Write postFixes
		// size
		VarNumberSerializer.writeVarInt(serializer, 0);
		// then one string per index

		// Write cmdEnums, a way to group the enumValues that can be refered to from
		// aliases, or from parameter types.
		// size
		VarNumberSerializer.writeVarInt(serializer, 0);
		// For each cmdEnum, a complex structure
		// String : name
		// VarInt : enum size
		// 0..size - enum values. Of type byte, short or int if enumValuesSize (first int written!)
		// is max 256, max 64k or larger.

		// Write commandData
		// size
		System.out.println("HELLO + WORLD");
		VarNumberSerializer.writeVarInt(serializer, 2);
		StringSerializer.writeVarIntUTF8String(serializer, "hello");
		StringSerializer.writeVarIntUTF8String(serializer, "writes hello");
		serializer.writeByte(0);
		serializer.writeByte(0);

		serializer.writeIntLE(-1); // alias

		// always has one overload.
		VarNumberSerializer.writeVarInt(serializer, 1);
		VarNumberSerializer.writeVarInt(serializer, 0); // no parameters

		StringSerializer.writeVarIntUTF8String(serializer, "world");
		StringSerializer.writeVarIntUTF8String(serializer, "writes YOU");
		serializer.writeByte(0);
		serializer.writeByte(0);

		serializer.writeIntLE(-1); // alias

		// always has one overload.
		VarNumberSerializer.writeVarInt(serializer, 1);
		VarNumberSerializer.writeVarInt(serializer, 0); // no parameters

		// For each commandData, a complex structure
		// String : name
		// String : description
		// byte : flags (always 0?)
		// byte : permission (always 0?)

		// LEint : alias index in enums, or -1 for none.

		// VarInt : size of overloads
		// for each overload:
		// --- VarInt : length of parameters
		//     for each parameter:
		//     String : parameter name
		//     LEint : messed-up-flag*
		//     byte : is optional (1 = true, 0 = false)
		//     byte : flags (?) -- always 0 in Nukkit

		// Messed up flags work like:
		//     public static final int ARG_FLAG_VALID = 0x100000;
		//    public static final int ARG_FLAG_ENUM = 0x200000;
		//    public static final int ARG_FLAG_POSTFIX = 0x1000000;

		// this is OR:ed on flag as follows:
		// if it has postfix, set postfix flag and also OR in index in postfix array
		// otherwise, ALWAYS add VALID with OR. Then also do one of following:
		// if the parameter has an enum add ENUM with OR, and also OR in the index in enums
		// otherwise OR in parameter type ID.

		// Parameter type IDs are here:
		//     public static final int ARG_TYPE_INT = 1;
		//    public static final int ARG_TYPE_FLOAT = 2;
		//    public static final int ARG_TYPE_VALUE = 3;
		//    public static final int ARG_TYPE_WILDCARD_INT = 4;
		//    public static final int ARG_TYPE_OPERATOR = 5;
		//    public static final int ARG_TYPE_TARGET = 6;
		//    public static final int ARG_TYPE_WILDCARD_TARGET = 7;
		//
		//    public static final int ARG_TYPE_FILE_PATH = 15;
		//
		//    public static final int ARG_TYPE_INT_RANGE = 19;
		//
		//    public static final int ARG_TYPE_STRING = 28;
		//    public static final int ARG_TYPE_POSITION = 30;
		//
		//    public static final int ARG_TYPE_MESSAGE = 33;
		//    public static final int ARG_TYPE_RAWTEXT = 35;
		//    public static final int ARG_TYPE_JSON = 38;
		//    public static final int ARG_TYPE_COMMAND = 45;


		// Write softEnums
		// size
		VarNumberSerializer.writeVarInt(serializer, 0);
		// then for each soft enum:
		// String : name
		// VarInt : number of soft enum values
		// --- and for each soft enum value:
		//     String : "value name"

		return serializer;
	}


	@Override
	public RecyclableCollection<ClientBoundPacketData> toData() {
		System.out.println("GETTING DECLARE COMMANDS");
		return RecyclableSingletonList.create(create());

		// return RecyclableEmptyList.get();

		// what to do
		/*
		ett COMMAND har:
		description
		alias (eller inte)
		overloads (en map)

		----
		först kommer enumValues, alla literaler som ska användas som argument, men EJ kommandon!!

		Sen kommer postFixes. Den är 0 just nu, tom.

		Sen kommer "enums", det är  24 stycken typ:
NAME: tellAliases
values.size(): 3
value: 11
value: 12
value: 13

NAME: gamemodeAliases
values.size(): 2
value: 20
value: 21

or

NAME: subtitleEnums
values.size(): 1
value: 15
NAME: clearEnums
values.size(): 1
value: 16

The latter means, I think, that "subtitle" and "clear" are literal arguments.

Finally also Types:
NAME: itemType
values.size(): 0

NAME: enchantmentType
values.size(): 0

only these two...

Then comes commands, 42 in total:
NAME: defaultgamemode
DESC: Set the default gamemode
FLAGS: 0
PERM: 0

NAME: weather
DESC: Sets the weather in current world
FLAGS: 0
PERM: 0

Simplast möjliga, typ:
NAME: plugins
DESC: Gets a list of plugins running on the server
FLAGS: 0
PERM: 0
aliases: 1
data.overloads.size(): 1
---
OVERLOAD.input.parameters.length: 0

måste alltid ha overload minst size 1, och sen kan den vara length 0.

alias 1 = enum nr 1 innehåller aliasarna.

Om man tar två arg:
NAME: tell
DESC: Sends a private message to the given player
FLAGS: 0
PERM: 0
aliases: 2
data.overloads.size(): 1
---
OVERLOAD.input.parameters.length: 2
NAME: player
TYPE: BASIC 6
type: 1048582
optional false
NAME: message
TYPE: BASIC 35
type: 1048611
optional false

Om man har flera olika sätt att ange parametrar, så anger man flera OVERLOADS. tänk på det som
överlagrade metoder i java.
så här:

NAME: tp
DESC: Teleports the given player (or yourself) to another player or coordinates
FLAGS: 0
PERM: 0
aliases: -1
data.overloads.size(): 4
---
OVERLOAD.input.parameters.length: 1
NAME: blockPos
TYPE: BASIC 30
type: 1048606
optional false
OVERLOAD.input.parameters.length: 2
NAME: player
TYPE: BASIC 6
type: 1048582
optional false
NAME: blockPos
TYPE: BASIC 30
type: 1048606
optional false
OVERLOAD.input.parameters.length: 1
NAME: player
TYPE: BASIC 6
type: 1048582
optional false
OVERLOAD.input.parameters.length: 2
NAME: player
TYPE: BASIC 6
type: 1048582
optional false
NAME: target
TYPE: BASIC 6
type: 1048582
optional false


slutligen, softEnums.size(): 0. är alltid tom.

.------


		enumValues är en lista av strängar av alla kommandon och aliasar.

		postfixes är en lista av strängar av alla postfix??? vad är detta? kan nog vara tom om vi inte har några.

        this.putUnsignedVarInt(enumValues.size());
        enumValues.forEach(this::putString);

        this.putUnsignedVarInt(postFixes.size());
        postFixes.forEach(this::putString);

        sen:

              ObjIntConsumer<BinaryStream> indexWriter;
        if (enumValues.size() < 256) {
            indexWriter = WRITE_BYTE;
        } else if (enumValues.size() < 65536) {
            indexWriter = WRITE_SHORT;
        } else {
            indexWriter = WRITE_INT;
        }

        this.putUnsignedVarInt(enums.size());
        enums.forEach((cmdEnum) -> {
            putString(cmdEnum.getName());

            List<String> values = cmdEnum.getValues();
            putUnsignedVarInt(values.size());
            for (String val : values) {
                int i = enumValues.indexOf(val);

                if (i < 0) {
                    throw new IllegalStateException("Enum value '" + val + "' not found");
                }

                indexWriter.accept(this, i);
            }


            så här funkar parametrar:
            say:
                    this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("message")

                setspawn:

                    this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("blockPos", CommandParamType.POSITION, true),

varje kommando har en commandParameters som är en array av olika sätt att anropa kommandot.
varje position i arrayen är en struktur som har ett namn och är en lista av parametrar: [namn, typ, optional].

dessa kallas även overloads:
        this.commandParameters.forEach((key, par) -> {
            CommandOverload overload = new CommandOverload();
            overload.input.parameters = par;
            customData.overloads.put(key, overload);
        });

        });




---
PC argument types:

BASIC TYPES!!!

        ArgumentRegistry.a(new MinecraftKey("brigadier:bool"), BoolArgumentType.class, new ArgumentSerializerVoid(BoolArgumentType::bool));
        ArgumentRegistry.a(new MinecraftKey("brigadier:float"), FloatArgumentType.class, new ArgumentSerializerFloat());
        ArgumentRegistry.a(new MinecraftKey("brigadier:double"), DoubleArgumentType.class, new ArgumentSerializerDouble());
        ArgumentRegistry.a(new MinecraftKey("brigadier:integer"), IntegerArgumentType.class, new ArgumentSerializerInteger());
        ArgumentRegistry.a(new MinecraftKey("brigadier:string"), StringArgumentType.class, new ArgumentSerializerString());


        a(new MinecraftKey("minecraft:entity"), ArgumentEntity.class, new net.minecraft.server.v1_13_R2.ArgumentEntity.a());
        a(new MinecraftKey("minecraft:game_profile"), ArgumentProfile.class, new ArgumentSerializerVoid(ArgumentProfile::a));
        a(new MinecraftKey("minecraft:block_pos"), ArgumentPosition.class, new ArgumentSerializerVoid(ArgumentPosition::a));
        a(new MinecraftKey("minecraft:column_pos"), ArgumentVec2I.class, new ArgumentSerializerVoid(ArgumentVec2I::a));
        a(new MinecraftKey("minecraft:vec3"), ArgumentVec3.class, new ArgumentSerializerVoid(ArgumentVec3::a));
        a(new MinecraftKey("minecraft:vec2"), ArgumentVec2.class, new ArgumentSerializerVoid(ArgumentVec2::a));
        a(new MinecraftKey("minecraft:block_state"), ArgumentTile.class, new ArgumentSerializerVoid(ArgumentTile::a));
        a(new MinecraftKey("minecraft:block_predicate"), ArgumentBlockPredicate.class, new ArgumentSerializerVoid(ArgumentBlockPredicate::a));
        a(new MinecraftKey("minecraft:item_stack"), ArgumentItemStack.class, new ArgumentSerializerVoid(ArgumentItemStack::a));
        a(new MinecraftKey("minecraft:item_predicate"), ArgumentItemPredicate.class, new ArgumentSerializerVoid(ArgumentItemPredicate::a));
        a(new MinecraftKey("minecraft:color"), ArgumentChatFormat.class, new ArgumentSerializerVoid(ArgumentChatFormat::a));
        a(new MinecraftKey("minecraft:component"), ArgumentChatComponent.class, new ArgumentSerializerVoid(ArgumentChatComponent::a));
        a(new MinecraftKey("minecraft:message"), ArgumentChat.class, new ArgumentSerializerVoid(ArgumentChat::a));
        a(new MinecraftKey("minecraft:nbt"), ArgumentNBTTag.class, new ArgumentSerializerVoid(ArgumentNBTTag::a));
        a(new MinecraftKey("minecraft:nbt_path"), ArgumentNBTKey.class, new ArgumentSerializerVoid(ArgumentNBTKey::a));
        a(new MinecraftKey("minecraft:objective"), ArgumentScoreboardObjective.class, new ArgumentSerializerVoid(ArgumentScoreboardObjective::a));
        a(new MinecraftKey("minecraft:objective_criteria"), ArgumentScoreboardCriteria.class, new ArgumentSerializerVoid(ArgumentScoreboardCriteria::a));
        a(new MinecraftKey("minecraft:operation"), ArgumentMathOperation.class, new ArgumentSerializerVoid(ArgumentMathOperation::a));
        a(new MinecraftKey("minecraft:particle"), ArgumentParticle.class, new ArgumentSerializerVoid(ArgumentParticle::a));
        a(new MinecraftKey("minecraft:rotation"), ArgumentRotation.class, new ArgumentSerializerVoid(ArgumentRotation::a));
        a(new MinecraftKey("minecraft:scoreboard_slot"), ArgumentScoreboardSlot.class, new ArgumentSerializerVoid(ArgumentScoreboardSlot::a));
        a(new MinecraftKey("minecraft:score_holder"), ArgumentScoreholder.class, new c());
        a(new MinecraftKey("minecraft:swizzle"), ArgumentRotationAxis.class, new ArgumentSerializerVoid(ArgumentRotationAxis::a));
        a(new MinecraftKey("minecraft:team"), ArgumentScoreboardTeam.class, new ArgumentSerializerVoid(ArgumentScoreboardTeam::a));
        a(new MinecraftKey("minecraft:item_slot"), ArgumentInventorySlot.class, new ArgumentSerializerVoid(ArgumentInventorySlot::a));
        a(new MinecraftKey("minecraft:resource_location"), ArgumentMinecraftKeyRegistered.class, new ArgumentSerializerVoid(ArgumentMinecraftKeyRegistered::a));
        a(new MinecraftKey("minecraft:mob_effect"), ArgumentMobEffect.class, new ArgumentSerializerVoid(ArgumentMobEffect::a));
        a(new MinecraftKey("minecraft:function"), ArgumentTag.class, new ArgumentSerializerVoid(ArgumentTag::a));
        a(new MinecraftKey("minecraft:entity_anchor"), ArgumentAnchor.class, new ArgumentSerializerVoid(ArgumentAnchor::a));
        a(new MinecraftKey("minecraft:int_range"), b.class, new net.minecraft.server.v1_13_R2.ArgumentCriterionValue.b.a());
        a(new MinecraftKey("minecraft:float_range"), net.minecraft.server.v1_13_R2.ArgumentCriterionValue.a.class, new net.minecraft.server.v1_13_R2.ArgumentCriterionValue.a.a());
        a(new MinecraftKey("minecraft:item_enchantment"), ArgumentEnchantment.class, new ArgumentSerializerVoid(ArgumentEnchantment::a));
        a(new MinecraftKey("minecraft:entity_summon"), ArgumentEntitySummon.class, new ArgumentSerializerVoid(ArgumentEntitySummon::a));
        a(new MinecraftKey("minecraft:dimension"), ArgumentDimension.class, new ArgumentSerializerVoid(ArgumentDimension::a));

---

På PC sidan finns grunden i com.mojang.brigadier.tree.CommandNode!

		 */
	}

}
