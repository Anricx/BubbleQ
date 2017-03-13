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
        
         
PUSH => S:byte(mode)utf(from)[utf(flags)utf(topic)utf(msgid)playload]
     => none
PUSH-mode => {
	PUBLISH((byte) 0x00),
	LISTENING((byte) 0x10),
	RPC_REQ((byte) 0x02),
}

REC_REQ
flags
 ----------------------------------------------------------
 | bit    |        7       |   | 5    4 |   3    |  2    1   |0      |
 ----------------------------------------------------------
 | byte 1 | Require Response |   |                           |
 ----------------------------------------------------------
=> C:utf(flags)utf(target)utf(msgid)playload
=> S:byte(status)utf(target)utf(msgid)playload

REC_RESP
flags
 ----------------------------------------------------------
 | bit    |        7       |   | 5    4 |   3    |  2    1   |0      |
 ----------------------------------------------------------
 | byte 1 |  |   |                           |
 ----------------------------------------------------------
=> C:utf(flags)utf(target)utf(msgid)playload
=> S:byte(status)utf(target)utf(msgid)playload


logger
=======
[Transfer][O] => Connection Open
[Transfer][I] => Connection
[Transfer][C] => Connection Closed
[Transfer][E] => Error Occur
[Transfer][R] => Socket Date Read
[Transfer][W] => Socket Date Writen

[Bubble][H] => Client HELLO Protocol
[Bubble][S] => Client SUBSCRIBE Protocol
[Bubble][L] => Client LISTEN Protocol
[Bubble][P] => Client PUBLISH Protocol
[Bubble][U] => Client PUSH Protocol
[Bubble][-] => Client PING Protocol
[Bubble][C] => Client RPC Protocol
[Bubble][B] => Client BYE Protocol
[Bubble][*] => Client Whisper Protocol