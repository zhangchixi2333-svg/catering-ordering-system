package org.example.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.notificationservice.entity.ChatMessage;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select({
            "<script>",
            "SELECT DISTINCT m.*",
            "FROM chat_message m",
            "JOIN chat_message_recipient r ON m.message_id = r.message_id",
            "WHERE (",
            "  (m.sender_id = #{currentUserId} AND r.recipient_id IN",
            "    <foreach collection='peerIds' item='peerId' open='(' separator=',' close=')'>#{peerId}</foreach>",
            "  )",
            "  OR",
            "  (m.sender_id IN",
            "    <foreach collection='peerIds' item='peerId' open='(' separator=',' close=')'>#{peerId}</foreach>",
            "   AND r.recipient_id = #{currentUserId})",
            ")",
            "ORDER BY m.send_time DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<ChatMessage> findConversationMessages(
            @Param("currentUserId") Long currentUserId,
            @Param("peerIds") List<Long> peerIds,
            @Param("limit") Integer limit);

    @Select("""
            SELECT DISTINCT m.*
            FROM chat_message m
            JOIN chat_message_recipient r ON m.message_id = r.message_id
            WHERE m.sender_id = #{currentUserId}
               OR r.recipient_id = #{currentUserId}
            ORDER BY m.send_time DESC
            LIMIT #{limit}
            """)
    List<ChatMessage> findRecentMessages(
            @Param("currentUserId") Long currentUserId,
            @Param("limit") Integer limit);
}
