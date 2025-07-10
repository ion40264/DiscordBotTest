package bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bot.entity.AllianceMember;

@Repository
public interface  AllianceMemberRepository extends JpaRepository<AllianceMember, Long>{
	public AllianceMember findByAyarabuName(String ayarabuName);
	public AllianceMember findByDiscordName(String discordName);
}
