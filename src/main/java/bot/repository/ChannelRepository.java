package bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bot.entity.Channel;

@Repository
public interface  ChannelRepository extends JpaRepository<Channel, Long>{
	public Channel findByChannelId(String channelId);
}
