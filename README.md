## BubbleQ

基础服务：消息中间件。

protocol
=======
BubbleQ v1.0 Protocol Specification

1. MQTT Control Packet format
##############################################
The message header for each BubbleQ command message contains a fixed header. The table below shows the fixed header format. 
 ----------------------------------------------------------
 | bit    | 7    6    5    4 |   3    |  2    1   |0      |
 ----------------------------------------------------------
 | byte 1 |  Message Type    |                            |
 | byte 2 |  Remaining Length                             |
 ----------------------------------------------------------

2. MQTT Control Packets
#############################################
HELLO => C:utf(name)utf(pass)
      => S:byte(status)utf(identifier)

SUBSCRIBE => C:utf(topic)
          => S:byte(status)utf(topic)
LISTEN => C:utf(topic)
          => S:byte(status)utf(topic)
          
PUBLISH 
flags
 ----------------------------------------------------------
 | bit    |        7       |   | 5    4 |   3    |  2    1   |0      |
 ----------------------------------------------------------
 | byte 1 | Broadcast Flag |   |                           |
 ----------------------------------------------------------
=> C:utf(flags)utf(topic)utf(msgid)playload
=> S:byte(status)utf(topic)utf(msgid)
        

FEEDBACK => C:utf(flags)utf(target)utf(topic)utf(msgid)playload
         => S:byte(status)utf(target)utf(topic)utf(msgid)
         
PUSH => S:byte(mode)utf(from)[utf(flags)utf(topic)utf(msgid)playload]
     => none
PUSH-mode => {
	PUBLISH((byte) 0x00),
	LISTENING((byte) 0x10),
	FEEDBACK((byte) 0x01);
}

logger
=======
[Bubble][O] => Connection Open
[Bubble][I] => Connection
[Bubble][C] => Connection Closed
[Bubble][E] => Error Occur
[Bubble][R] => Socket Date Read
[Bubble][W] => Socket Date Writen

[Bubble][H] => Client HELLO Protocol
[Bubble][S] => Client SUBSCRIBE Protocol
[Bubble][L] => Client LISTEN Protocol
[Bubble][P] => Client PUBLISH Protocol
[Bubble][F] => Client FEEDBACK Protocol
[Bubble][U] => Client PUSH Protocol
[Bubble][N] => Client PING Protocol
[Bubble][B] => Client BYE Protocol