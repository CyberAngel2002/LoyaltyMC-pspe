package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_pe;

import io.netty.buffer.ByteBuf;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleDeclareCommands;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.utils.Utils;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclareCommands extends MiddleDeclareCommands {

	private static final byte FLAG_IS_LITERAL = 1;
	private static final byte FLAG_IS_ARGUMENT = 2;
	private static final byte FLAG_IS_COMMAND_PATH_END = 4;
	private static final byte FLAG_HAS_REDIRECT = 8;
	private static final byte FLAG_HAS_SUGGESTION = 16;

	private static final byte FLAG_HAS_MIN_VALUE = 1;
	private static final byte FLAG_HAS_MAX_VALUE = 2;

	// Different kinds of string types. Not used here; documented for posterity.
	private static final byte STRING_IS_WORD = 1;
	private static final byte STRING_IS_PHRASE = 2;
	private static final byte STRING_IS_GREEDY = 3;

	// Flags for different kinds of entities. Not used here; documented for posterity.
	private static final byte FLAG_ENTITY_AMOUNT_IS_SINGLE = 1;
	private static final byte FLAG_ENTITY_TYPE_IS_PLAYER = 2;

	// PE argument types that we use. These do not match values used by Nukkit, since they did
	// not work for us. The reason for this is unclear. Many different values produced seemingly
	// the same data type in the PE client chat GUI. Documented here for posterity:
	// 8-13 target (with wildcard)
	// 14-17 file path
	// 18-26 "unknown"
	// 27-28 string
	// 29-31 position ("x y z")
	// 32-33 message
	// 34-36 text
	// 37-43 json
	// 44-... ? (tested up to 99) command
	public static final int ARG_TYPE_INT = 1;
	public static final int ARG_TYPE_FLOAT = 2;
	public static final int ARG_TYPE_TARGET = 6;
	public static final int ARG_TYPE_STRING = 27;
	public static final int ARG_TYPE_POSITION = 29;
	public static final int ARG_TYPE_MESSAGE = 32;
	// Not sure if we will need these..?
	public static final int ARG_TYPE_RAWTEXT = 34;
	public static final int ARG_TYPE_JSON = 37;
	public static final int ARG_TYPE_COMMAND = 44;

	public static final int ARG_FLAG_VALID = 0x100000;
	public static final int ARG_FLAG_ENUM = 0x200000;

	private CommandNode[] allNodes;
	private int rootNodeIndex;
	private CommandNode[] topLevelNodes;

	public DeclareCommands(ConnectionImpl connection) {
		super(connection);
	}

	public static class CommandNode {
		private String name;
		private String argType;
		private String suggestion;
		private int[] children;
		private int redirect;
		private boolean isPathEnd;

		public CommandNode(String name, String argType, String suggestion, int[] children, int redirect, boolean isPathEnd) {
			this.name = name;
			this.argType = argType;
			this.suggestion = suggestion;
			this.children = children;
			this.redirect = redirect;
			this.isPathEnd = isPathEnd;
		}

		@Override
		public String toString() {
			return Utils.toStringAllFields(this);
		}

		public boolean isLeaf() {
			return this.children.length == 0;
		}
	}

	public static class PECommandNode {
		private String name;
		private String argType;
		private int nameIndex;

		public PECommandNode(String name, String argType, Map<String, Integer> enumIndex) {
			this.name = name;
			this.argType = argType;

			// Cache enum index
			Integer enumPos = enumIndex.get(name);
			int index;
			if (enumPos == null) {
				index = enumIndex.size();
				enumIndex.put(name, index);
			} else {
				index = enumPos;
			}

			this.nameIndex = index;
		}
	}

	public static class PECommandsStructure {
		private String[] enumArray;
		private Map<CommandNode, List<List<PECommandNode>>> allOverloads;

		public PECommandsStructure(String[] enumArray, Map<CommandNode, List<List<PECommandNode>>> allOverloads) {
			this.enumArray = enumArray;
			this.allOverloads = allOverloads;
		}
	}

	@Override
	public void readFromServerData(ByteBuf from) {
		// In theory, we could read this in the superclass. However, right now only PE needs this data, so save us
		// the trouble of parsing it for everyone else by doing it here.
		int length = VarNumberSerializer.readVarInt(from);

		allNodes = new CommandNode[length];
		for (int i = 0; i < length; i++) {
			CommandNode node = readCommandNode(from);
			allNodes[i] = node;
		}

		rootNodeIndex = VarNumberSerializer.readVarInt(from);

		// For convenience, store all top-level nodes in a separate array. (These correspond to the
		// actual "commands", i.e. the first literal, like "weather").
		int numTopLevelNodes = allNodes[rootNodeIndex].children.length;
		int i = 0;
		topLevelNodes = new CommandNode[numTopLevelNodes];
		for (int index :  allNodes[rootNodeIndex].children) {
			topLevelNodes[i++] = allNodes[index];
		}
	}

	private CommandNode readCommandNode(ByteBuf from) {
		byte flags = from.readByte();
		boolean isPathEnd;
		int redirect;
		int[] children = ArraySerializer.readVarIntVarIntArray(from).clone();
		if ((flags & FLAG_HAS_REDIRECT) != 0) {
			redirect = VarNumberSerializer.readVarInt(from);
		} else {
			redirect = -1;
		}
		String name;
		String argType;
		String suggestion;

		isPathEnd = ((flags & FLAG_IS_COMMAND_PATH_END) != 0);

		if ((flags & FLAG_IS_LITERAL) != 0) {
			name = StringSerializer.readVarIntUTF8String(from);
			argType = null; // no argType signals this is a literal
			suggestion = null;
		} else if ((flags & FLAG_IS_ARGUMENT) != 0) {
			name = StringSerializer.readVarIntUTF8String(from);
			// This is slightly lossy; some arg types has extra information that we throw away
			// To properly encode that, we'd need a specialized class, not a string.
			argType = readArgType(from);

			if ((flags & FLAG_HAS_SUGGESTION) != 0) {
				suggestion = StringSerializer.readVarIntUTF8String(from);
			} else {
				suggestion = null;
			}
		} else {
			// This is only allowed for the root node
			name = null;
			argType = null;
			suggestion = null;
		}
		return new CommandNode(name, argType, suggestion, children, redirect, isPathEnd);
	}

	private String readArgType(ByteBuf from) {
		String argType = StringSerializer.readVarIntUTF8String(from);
		// Depending on argType, there might be additional data.
		// At this point, we're just throwing this away, but we need at least
		// skip over it in the buffer.

		if (argType.equals("brigadier:string")) {
			// Determine kind of string, any of STRING_IS_*...
			int stringType = VarNumberSerializer.readVarInt(from);
		} else if (argType.equals("brigadier:integer")) {
			byte flag = from.readByte();
			if ((flag & FLAG_HAS_MIN_VALUE) != 0) {
				int min = from.readInt();
			}
			if ((flag & FLAG_HAS_MAX_VALUE) != 0) {
				int max = from.readInt();
			}
		} else if (argType.equals("brigadier:float")) {
			byte flag = from.readByte();
			if ((flag & FLAG_HAS_MIN_VALUE) != 0) {
				float min = from.readFloat();
			}
			if ((flag & FLAG_HAS_MAX_VALUE) != 0) {
				float max = from.readFloat();
			}
		} else if (argType.equals("brigadier:double")) {
			byte flag = from.readByte();
			if ((flag & FLAG_HAS_MIN_VALUE) != 0) {
				double min = from.readDouble();
			}
			if ((flag & FLAG_HAS_MAX_VALUE) != 0) {
				double max = from.readDouble();
			}
		} else if (argType.equals("minecraft:entity")) {
			// The flag determines the amount (single or double) and type (players or entities)
			// See FLAG_ENTITY_AMOUNT_IS_SINGLE and FLAG_ENTITY_TYPE_IS_PLAYER.
			byte flag = from.readByte();
		} else if (argType.equals("minecraft:score_holder")) {
			// The "multiple" boolean is true if multiple, false if single.
			byte multiple = from.readByte();
		} else {
			// For all other types, there are no additional data. This might change in future versions of Minecraft.
		}
		return argType;
	}

	private int getPeVariableCode(String pcVariableName) {

		if (pcVariableName.equals("brigadier:bool")) {
			return ARG_TYPE_STRING;
		} else if (pcVariableName.equals("brigadier:float")) {
			return ARG_TYPE_FLOAT;
		} else if (pcVariableName.equals("brigadier:double")) {
			return ARG_TYPE_FLOAT;
		} else if (pcVariableName.equals("brigadier:integer")) {
			return ARG_TYPE_INT;
		} else if (pcVariableName.equals("brigadier:string")) {
			return ARG_TYPE_STRING;
		} else if (pcVariableName.equals("minecraft:int_range")) {
			return ARG_TYPE_INT;
		} else if (pcVariableName.equals("minecraft:float_range")) {
			return ARG_TYPE_FLOAT;
		} else if (pcVariableName.equals("minecraft:vec3")) {
			return ARG_TYPE_POSITION;
		} else if (pcVariableName.equals("minecraft:entity")) {
			return ARG_TYPE_TARGET;
		} else if (pcVariableName.equals("minecraft:message")) {
			return ARG_TYPE_MESSAGE;
		} else {
			// We have a specialized type in PC which has no correspondance in PE. Sucks!
			// Tried ARG_TYPE_RAWTEXT before, but that "swallows" everything to the end of the line
			return ARG_TYPE_STRING;
		}
	}

	private void walkNode(List<List<PECommandNode>> overloads, Map<String, Integer> enumIndex, CommandNode currentNode, List<CommandNode> previousNodes) {
		if (currentNode.isLeaf()) {
			List<PECommandNode> newOverload = new ArrayList<>();
			for (CommandNode node : previousNodes) {
				PECommandNode peNode = new PECommandNode(node.name, node.argType, enumIndex);
				newOverload.add(peNode);
			}
			overloads.add(newOverload);
		} else {
			for (int i = 0; i < currentNode.children.length; i++) {
				int childNodeIndex = currentNode.children[i];
				CommandNode childNode = allNodes[childNodeIndex];
				List<CommandNode> nodes = new ArrayList<>(previousNodes);
				nodes.add(childNode);
				walkNode(overloads, enumIndex, childNode, nodes);
			}
		}
	}

	private PECommandsStructure buildPEStructure() {
		// Collect mapping of enum string values to integers in enumIndex
		Map<String, Integer> enumIndex = new HashMap<>();
		String[] enumArray;
		Map<CommandNode, List<List<PECommandNode>>> allOverloads;

		allOverloads = new HashMap<>(topLevelNodes.length);
		for (CommandNode node : topLevelNodes) {
			List<List<PECommandNode>> overloads = new ArrayList<>();
			// HashSet<ArrayList<String>>();
			walkNode(overloads, enumIndex, node, new ArrayList<>());
			allOverloads.put(node, overloads);
		}

		// Convert enumIndex to proper array per index
		enumArray = new String[enumIndex.size()];
		for (Map.Entry<String, Integer> entry : enumIndex.entrySet()) {
			enumArray[entry.getValue()] = entry.getKey();
		}

		return new PECommandsStructure(enumArray, allOverloads);
	}

	public ClientBoundPacketData create(PECommandsStructure struct) {
		ClientBoundPacketData serializer = ClientBoundPacketData.create(PEPacketIDs.AVAILABLE_COMMANDS);

		// Write enumValues, a way to number strings
		// First size
		VarNumberSerializer.writeVarInt(serializer, struct.enumArray.length);
		// then one string per index
		for (String s : struct.enumArray) {
			StringSerializer.writeVarIntUTF8String(serializer, s);
		}

		// Write "postfixes". Start with the count. We don't have any, so ignore the structure.
		VarNumberSerializer.writeVarInt(serializer, 0);

		// Write cmdEnums, a way to group the enumValues that can be refered to from
		// aliases, or from parameter types.
		// We have a 1-to-1 match between enums and enumGroups.
		writeEnumGroups(serializer, struct.enumArray);

		// Now process the actual commands. Write on per top-level ("command") node.
		VarNumberSerializer.writeVarInt(serializer, topLevelNodes.length);
		for (CommandNode node : topLevelNodes) {
			StringSerializer.writeVarIntUTF8String(serializer, node.name);
			// PC does not have any description, so just send an empty string
			StringSerializer.writeVarIntUTF8String(serializer, "");
			serializer.writeByte(0); // Flags? Always 0.
			serializer.writeByte(0);  // Permissions? Always 0.

			// Enum index for our alias list, or -1 if none.
			serializer.writeIntLE(-1);

			// Write out all "overloads", i.e. all different ways to call this command with arguments.
			List<List<PECommandNode>> overloads = struct.allOverloads.get(node);

			// First write number of overloads for this command
			VarNumberSerializer.writeVarInt(serializer, overloads.size());

			for (List<PECommandNode> overload : overloads) {
				// For each overload, write out all arguments in order
				VarNumberSerializer.writeVarInt(serializer, overload.size());
				for (PECommandNode peNode : overload) {
					writePeNode(serializer, peNode);
				}
			}
		}

		// Write "soft enums". Start with the count. We don't have any, so ignore the structure.
		VarNumberSerializer.writeVarInt(serializer, 0);

		return serializer;
	}

	private void writePeNode(ClientBoundPacketData serializer, PECommandNode peNode) {
		int flag;
		if (peNode.argType != null) {
			// This is a variable; write the name and it's type
			StringSerializer.writeVarIntUTF8String(serializer, peNode.name);
			flag = getPeVariableCode(peNode.argType) | ARG_FLAG_VALID;
		} else {
			// This is a literal; write the corresponding enum constant.

			// The literal arguments also has a "name", but it's not used, so leave it empty.
			// (The actual value shown in the GUI is from the enum group)
			StringSerializer.writeVarIntUTF8String(serializer, "");

			// In theory, this is the index into the enumGroups, but we have the same index
			// to our single enum so we can use that without conversion.
			int index = peNode.nameIndex;
			flag = index | ARG_FLAG_VALID | ARG_FLAG_ENUM;
		}

		// The "flag" design is really... odd. Bedrock Edition engineers. Don't ask.
		serializer.writeIntLE(flag);
		serializer.writeByte(0); // Boolean IS_OPTIONAL (1 = true). For now, call everything compulsory.
		serializer.writeByte(0); // Flags? Always 0.
	}

	private void writeEnumGroups(ClientBoundPacketData serializer, String[] enumArray) {
		// First size
		VarNumberSerializer.writeVarInt(serializer, enumArray.length);
		for (int i = 0; i < enumArray.length; i++) {
			// Enum groups have a "name". It is never used, but needs to be unique.
			StringSerializer.writeVarIntUTF8String(serializer, "e" + i);
			// Number of enums in group, always just 1.
			VarNumberSerializer.writeVarInt(serializer, 1);
			// Serialize enum index by using minimal data type
			writeSingleEnum(serializer, i, enumArray.length);
		}
	}

	private void writeSingleEnum(ClientBoundPacketData serializer, int value, int maxValue) {
		if (maxValue < 256) {
			serializer.writeByte(value);
		} else if (maxValue < 65536) {
			serializer.writeShortLE(value);
		} else {
			serializer.writeIntLE(value);
		}
	}

	@Override
	public RecyclableCollection<ClientBoundPacketData> toData() {
		PECommandsStructure struct = buildPEStructure();

		return RecyclableSingletonList.create(create(struct));
	}
}

/*
TODO:
fix aliases. PC has redirect, PE has an EnumSet as alias.

add proper value for "optional" flag.

It could be that these Enums correspond to special types..?
NAME: itemType
values.size(): 0

NAME: enchantmentType
values.size(): 0

Idea: transform brigadier:bool to enum group "true|false". Maybe use soft enum for this?
 */

/*
ArgumentRegistry.a(new MinecraftKey("brigadier:bool"), BoolArgumentType.class, new ArgumentSerializerVoid(BoolArgumentType::bool));
ArgumentRegistry.a(new MinecraftKey("brigadier:float"), FloatArgumentType.class, new ArgumentSerializerFloat());
ArgumentRegistry.a(new MinecraftKey("brigadier:double"), DoubleArgumentType.class, new ArgumentSerializerDouble());
ArgumentRegistry.a(new MinecraftKey("brigadier:integer"), IntegerArgumentType.class, new ArgumentSerializerInteger());
ArgumentRegistry.a(new MinecraftKey("brigadier:string"), StringArgumentType.class, new ArgumentSerializerString());

a(new MinecraftKey("minecraft:entity"), ArgumentEntity.class, new net.minecraft.server.v1_13_R2.ArgumentEntity.a());
a(new MinecraftKey("minecraft:score_holder"), ArgumentScoreholder.class, new c());
a(new MinecraftKey("minecraft:int_range"), b.class, new net.minecraft.server.v1_13_R2.ArgumentCriterionValue.b.a());
a(new MinecraftKey("minecraft:float_range"), net.minecraft.server.v1_13_R2.ArgumentCriterionValue.a.class, new net.minecraft.server.v1_13_R2.ArgumentCriterionValue.a.a());

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
a(new MinecraftKey("minecraft:swizzle"), ArgumentRotationAxis.class, new ArgumentSerializerVoid(ArgumentRotationAxis::a));
a(new MinecraftKey("minecraft:team"), ArgumentScoreboardTeam.class, new ArgumentSerializerVoid(ArgumentScoreboardTeam::a));
a(new MinecraftKey("minecraft:item_slot"), ArgumentInventorySlot.class, new ArgumentSerializerVoid(ArgumentInventorySlot::a));
a(new MinecraftKey("minecraft:resource_location"), ArgumentMinecraftKeyRegistered.class, new ArgumentSerializerVoid(ArgumentMinecraftKeyRegistered::a));
a(new MinecraftKey("minecraft:mob_effect"), ArgumentMobEffect.class, new ArgumentSerializerVoid(ArgumentMobEffect::a));
a(new MinecraftKey("minecraft:function"), ArgumentTag.class, new ArgumentSerializerVoid(ArgumentTag::a));
a(new MinecraftKey("minecraft:entity_anchor"), ArgumentAnchor.class, new ArgumentSerializerVoid(ArgumentAnchor::a));
a(new MinecraftKey("minecraft:item_enchantment"), ArgumentEnchantment.class, new ArgumentSerializerVoid(ArgumentEnchantment::a));
a(new MinecraftKey("minecraft:entity_summon"), ArgumentEntitySummon.class, new ArgumentSerializerVoid(ArgumentEntitySummon::a));
a(new MinecraftKey("minecraft:dimension"), ArgumentDimension.class, new ArgumentSerializerVoid(ArgumentDimension::a));
*/