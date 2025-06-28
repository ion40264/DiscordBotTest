package bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bot.entity.DiscoMember;

@Repository
public interface  DiscoMemberRepository extends JpaRepository<DiscoMember, Long>{
	public DiscoMember findByName(String name);
}
