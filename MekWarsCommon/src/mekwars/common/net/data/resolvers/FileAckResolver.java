// /*
//  * MekWars - Copyright (C) 2025
//  * 
//  * This program is free software; you can redistribute it and/or modify it
//  * under the terms of the GNU General Public License as published by the Free
//  * Software Foundation; either version 2 of the License, or (at your option)
//  * any later version.
//  *
//  * This program is distributed in the hope that it will be useful, but
//  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
//  * for more details.
//  */

// package mekwars.common.net.data.resolvers;

// import mekwars.common.net.AbstractPacket;
// import mekwars.common.net.AbstractResolver;
// import mekwars.common.net.ConnectionHandler;
// import mekwars.common.net.data.DataConnection;
// import mekwars.common.net.data.contexts.FileWriteContext;
// import mekwars.common.net.data.packets.FileAck;
// import mekwars.common.net.data.packets.FileData;
// import mekwars.common.net.data.packets.PacketType;

// public class FileAckResolver extends AbstractResolver<FileAck, DataConnection> {
//     public FileAckResolver(ConnectionHandler handler) {
//         super(handler);
//     }

//     public void receive(FileAck message, DataConnection connection) {
//         FileWriteContext context = (FileWriteContext) connection.getContext();

//         if (!context.isComplete()) {
//             int blockLength = context.getBlockLength();
//             byte[] fileContents = context.getContent();
//             byte[] data = new byte[blockLength];

//             System.arraycopy(fileContents, context.getBlockStart(), data, 0, blockLength);
//             connection.write(new FileData(context.getCurrentBlock(), data));
//             context.nextBlock();
//         }
//     }

//     public boolean canResolve(AbstractPacket.Type packetType) {
//         return packetType.getType() == PacketType.FILE_ACK.getType();
//     }
// }
