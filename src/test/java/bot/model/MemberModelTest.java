package bot.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.dto.AllianceMemberDto;
import bot.repository.AllianceMemberRepository;

@Component 
class MemberModelTest {
	
	@Autowired
    private MemberModel memberModel;
	
    private AllianceMemberRepository repository;
	
    
	
	@Test
	void testRemoveAllianceMemberDto() {
		/*
		AllianceMemberDto removeDto = memberModel.getAllianceMemberDto(1L);
		allianceMemberDtoList.remove(removeDto);
        // DTOリストから削除されたか確認
		assertEquals(0, memberModel.getAllianceMemberDtoList().size());
		allianceMemberRepository.deleteById(1L);
		*/
		
	}

	@Test
	void testInit() {
        String name = "Fine";
        String discordId = "fine4139";
        memberModel.init(name, discordId, false);

        AllianceMemberDto allianceDto = memberModel.getAllianceMemberDto(discordId);
        assertNotNull(allianceDto);
        assertEquals(name, allianceDto.getDiscordName());
        assertEquals(discordId, allianceDto.getDiscordMemberId());
        assertEquals("メンバー", allianceDto.getMemberRole());
        assertEquals("ほっこり茶屋", allianceDto.getAlliance());
	}

	@Test
	void testGetAllianceMemberDtoList() {
		fail("まだ実装されていません");
	}

	@Test
	void testAddOrChangeAllianceMemberDto() {
		fail("まだ実装されていません");
	}

	@Test
	void testRemoveAllianceMemberDtoByDiscordId() {
		fail("まだ実装されていません");
	}

	@Test
	void testGetAllianceMemberDtoString() {
		fail("まだ実装されていません");
	}

	@Test
	void testGetAllianceMemberDtoLong() {
		fail("まだ実装されていません");
	}
	

	
}
