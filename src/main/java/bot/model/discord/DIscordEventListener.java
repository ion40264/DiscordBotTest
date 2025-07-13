package bot.model.discord;

import java.util.List;

import bot.dto.AllianceMemberDto;

public interface DIscordEventListener {
	public void onMessageReceived(AllianceMemberDto allianceMemberDto, String messageId, String message,
			String referencedMessageId,
			List<String> attachmentUrlList) ;
	public void onMessageUpdate(AllianceMemberDto allianceMemberDto, String messageId, String message,
			String referencedMessageId,
			List<String> attachmentUrlList) ;
	public void onMessageDelete(String messageId) ;
	public void onGuildMemberJoin(AllianceMemberDto allianceMemberDto) ;
	public void onGuildMemberRemove(AllianceMemberDto allianceMemberDto) ;

}
