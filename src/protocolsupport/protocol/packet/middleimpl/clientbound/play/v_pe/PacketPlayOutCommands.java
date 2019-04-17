//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_pe;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.v1_13_R2.ArgumentRegistry;
import net.minecraft.server.v1_13_R2.CompletionProviders;
import net.minecraft.server.v1_13_R2.ICompletionProvider;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketListenerPlayOut;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class PacketPlayOutCommands implements Packet<PacketListenerPlayOut> {
    private RootCommandNode<ICompletionProvider> rootCommandNode;

    public PacketPlayOutCommands() {
    }

    public PacketPlayOutCommands(RootCommandNode<ICompletionProvider> rootCommandNode) {
        this.rootCommandNode = rootCommandNode;
    }

    // read from serializer
    public void readEntirePacketFromSerializer(PacketDataSerializer serializer) throws IOException {
    	// g() is read VarInt
        CommandNode[] singleCommandsArray = new CommandNode[serializer.g()];
        ArrayDeque deque = new ArrayDeque(singleCommandsArray.length);

        for(int i = 0; i < singleCommandsArray.length; ++i) {
            singleCommandsArray[i] = this.readSingleCommand(serializer);
            deque.add(singleCommandsArray[i]);
        }

        boolean keepGoing;
        do {
            if (deque.isEmpty()) {
            	// finally, read a varint with the root command node
                this.rootCommandNode = (RootCommandNode)singleCommandsArray[serializer.g()].commandNode;
                return;
            }

            keepGoing = false;
            Iterator iterator = deque.iterator();

            while(iterator.hasNext()) {
                CommandNode singleCommandNode = (CommandNode)iterator.next();
                if (singleCommandNode.updateRootCommandNode(singleCommandsArray)) {
                    iterator.remove();
                    keepGoing = true;
                }
            }
        } while(keepGoing);

        throw new IllegalStateException("Server sent an impossible command tree");
    }

	@Override
	public void a(PacketDataSerializer packetDataSerializer) throws IOException {
    	// this is the real intention of the inherited "a()" method:
		readEntirePacketFromSerializer(packetDataSerializer);
	}

	@Override
	public void b(PacketDataSerializer packetDataSerializer) throws IOException {
		// this is the real intention of the inherited "b()" method:
		writeEntirePacketToSerializer(packetDataSerializer);
	}

	public void writeEntirePacketToSerializer(PacketDataSerializer serializer) throws IOException {
        HashMap allCommandNodes = Maps.newHashMap();
        ArrayDeque deque = new ArrayDeque();
        deque.add(this.rootCommandNode);

        // take the tree rooted in rootCommandNode and add all children to the allCommandNodes map.
		// the deque keeps track on command nodes still not processed. The allCommandNodes map contains
		// a amapping from CommandNode to the number of elements in the map at the time the node was added,
		// basically giving each command an int sequence number.
		// For each node, all Children and all Redirects (is that aliases?) are added.
        while(!deque.isEmpty()) {
            com.mojang.brigadier.tree.CommandNode commandNode = (com.mojang.brigadier.tree.CommandNode)deque.pollFirst();
            if (!allCommandNodes.containsKey(commandNode)) {
                int size = allCommandNodes.size();
                allCommandNodes.put(commandNode, size);
                deque.addAll(commandNode.getChildren());
                if (commandNode.getRedirect() != null) {
                    deque.add(commandNode.getRedirect());
                }
            }
        }

        // fill commandNodeArray according to:
		// use position from map.value() and store in it map.key()
        com.mojang.brigadier.tree.CommandNode[] commandNodeArray = (com.mojang.brigadier.tree.CommandNode[])(new com.mojang.brigadier.tree.CommandNode[allCommandNodes.size()]);

        Entry entry;
        for(Iterator iterator = allCommandNodes.entrySet().iterator(); iterator.hasNext(); ) {
			entry = (Entry)iterator.next();
			commandNodeArray[(Integer)entry.getValue()] = (com.mojang.brigadier.tree.CommandNode)entry.getKey();
        }

        // At this point, the commandNodeArray should contain a unique sequence of all command Nodes.

        // write out the command node length. Write VarInt, I think.
        serializer.d(commandNodeArray.length);
        com.mojang.brigadier.tree.CommandNode[] commandNodeArray2 = commandNodeArray;
        int commandNodeArrayLength = commandNodeArray.length;

        for(int i = 0; i < commandNodeArrayLength; ++i) {
            com.mojang.brigadier.tree.CommandNode commandNode = commandNodeArray2[i];
            // for each node, write it out.
            this.writeCommandNodeToSerializer(serializer, commandNode, allCommandNodes);
        }

        // write VarInt, the position of the rootCommandNode in the array.
        serializer.d((Integer)allCommandNodes.get(this.rootCommandNode));
    }

    private CommandNode readSingleCommand(PacketDataSerializer serializer) {
    	// read flags, a byte
        byte flags = serializer.readByte();
        // read int array, this is all children
        int[] commandStartPositions = serializer.b();
        // flags & 8 == has redirect
		// g() is read varint. skip reading if no redirect.
        int redirect = (flags & 8) != 0 ? serializer.g() : 0;

        ArgumentBuilder argumentBuilder = this.readArgumentBuilderFromSerializer(serializer, flags);
        return new CommandNode(argumentBuilder, flags, redirect, commandStartPositions);
    }

    @Nullable
    private ArgumentBuilder<ICompletionProvider, ?> readArgumentBuilderFromSerializer(PacketDataSerializer serializer, byte flag) {
        int cmdType = flag & 3;
        if (cmdType == 2) {
        	// read string
            String name = serializer.e(32767);
            // a() is read from serialize, input from serializer is string e.g. "minecraft:pos" or whateer.
            ArgumentType type = ArgumentRegistry.a(serializer);
            if (type == null) {
            	// fail!!!
                return null;
            } else {
                RequiredArgumentBuilder argumentBuilder = RequiredArgumentBuilder.argument(name, type);
                if ((flag & 16) != 0) {
                	// flag 16 = we have suggestion
					// l() = read MinecraftKey, which is a string
                    argumentBuilder.suggests(CompletionProviders.a(serializer.l()));
                }

                return argumentBuilder;
            }
        } else {
        	// read string
            return cmdType == 1 ? LiteralArgumentBuilder.literal(serializer.e(32767)) : null;
        }
    }

    private void writeCommandNodeToSerializer(PacketDataSerializer serializer, com.mojang.brigadier.tree.CommandNode commandNode, Map<com.mojang.brigadier.tree.CommandNode, Integer> allCommandNodes) {
        byte flags = 0;
        if (commandNode.getRedirect() != null) {
        	// flag that we have aliases
            flags = (byte)(flags | 8);
        }

        if (commandNode.getCommand() != null) {
        	// flag that we have a command (???)
            flags = (byte)(flags | 4);
        }

        if (commandNode instanceof RootCommandNode) {
            flags = (byte)(flags | 0);
        } else if (commandNode instanceof ArgumentCommandNode) {
        	// flag that we have arguments
            flags = (byte)(flags | 2);
            if (((ArgumentCommandNode)commandNode).getCustomSuggestions() != null) {
            	// flag that we have custom suggestions
                flags = (byte)(flags | 16);
            }
        } else {
            if (!(commandNode instanceof LiteralCommandNode)) {
                throw new UnsupportedOperationException("Unknown node type " + commandNode);
            }

            // flag that we are literal command node
            flags = (byte)(flags | 1);
        }
        // we need to be either root (flag = 0), literal (flag = 1), arguments (flag = 2). must be one of these.
		// we can also optionally have command (flag =4), aliases (flag=8), custom suggestions (flag=16, only
		// if we have arguments).

        serializer.writeByte(flags);
        // write varint, number of children
        serializer.d(commandNode.getChildren().size());
        Iterator iterator = commandNode.getChildren().iterator();

        while(iterator.hasNext()) {
            com.mojang.brigadier.tree.CommandNode commandNode1 = (com.mojang.brigadier.tree.CommandNode)iterator.next();
            // the integer we get is the command-number of all children.
			// write varint
            serializer.d((Integer) allCommandNodes.get(commandNode1));
        }

        if (commandNode.getRedirect() != null) {
        	// write varint, with the single redirect command number, if any.
			// must match flag.
            serializer.d((Integer) allCommandNodes.get(commandNode.getRedirect()));
        }

        // it's either literal or argument (or root...)
        if (commandNode instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode;
            // write string
            serializer.a(argumentCommandNode.getName());
            // write argument type, as a string, e.g. "minecraft:entity"
            ArgumentRegistry.a(serializer, argumentCommandNode.getType());
            if (argumentCommandNode.getCustomSuggestions() != null) {
				// getCustomSuggestions returns SuggestionProvider<S>
				// CompletionProviders.a() returns a MinecraftKey.
				// by default this is "minecraft:ask_server", but it could be specialized.
				// we must have set the flag for this!
				// write string
                serializer.a(CompletionProviders.a(argumentCommandNode.getCustomSuggestions()));
            }
        } else if (commandNode instanceof LiteralCommandNode) {
        	// write string
            serializer.a(((LiteralCommandNode)commandNode).getLiteral());
        }

    }

    public void a(PacketListenerPlayOut listener) {
    	// add ourself to listener
        // listener.a(this);
    }

    static class CommandNode {
        @Nullable
        private final ArgumentBuilder<ICompletionProvider, ?> argumentBuilder;
        private final byte flags;
        private final int redirect;
        private final int[] commandStartPositions;
        private com.mojang.brigadier.tree.CommandNode commandNode;

        private CommandNode(@Nullable ArgumentBuilder<ICompletionProvider, ?> argumentBuilder, byte flags, int redirect, int[] commandStartPositions) {
            this.argumentBuilder = argumentBuilder;
            this.flags = flags;
            this.redirect = redirect;
            this.commandStartPositions = commandStartPositions;
        }

        public boolean updateRootCommandNode(CommandNode[] commands) {
            if (this.commandNode == null) {
                if (this.argumentBuilder == null) {
                    this.commandNode = new RootCommandNode();
                } else {
                    if ((this.flags & 8) != 0) {
                    	// flags 8 = has redirect
                        if (commands[this.redirect].commandNode == null) {
                            return false;
                        }

                        this.argumentBuilder.redirect(commands[this.redirect].commandNode);
                    }

                    if ((this.flags & 4) != 0) {
                    	// flag 4 == has command
                        this.argumentBuilder.executes((arg) -> {
                            return 0;
                        });
                    }

                    this.commandNode = this.argumentBuilder.build();
                }
            }

            int[] commandStartPositions = this.commandStartPositions;
            int length = commandStartPositions.length;

            int i;
            int pos;
            for(i = 0; i < length; ++i) {
                pos = commandStartPositions[i];
                if (commands[pos].commandNode == null) {
                	// fail if there is no command here
                    return false;
                }
            }

            // Add all RootCommandNodes as children to this.commandNode.
			commandStartPositions = this.commandStartPositions;
            length = commandStartPositions.length;

            for(i = 0; i < length; ++i) {
                pos = commandStartPositions[i];
                com.mojang.brigadier.tree.CommandNode commandNode = commands[pos].commandNode;
                if (!(commandNode instanceof RootCommandNode)) {
                    this.commandNode.addChild(commandNode);
                }
            }

            return true;
        }
    }
}
