package demo.util;

/***
 *  @Description: TODO(ID生成类，生成适用于各类场景的ID)
 *  @author xiaofang
 * @Date:2019-3-24 22:25
 */
public class IdUtil {
    private final static SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);
    /**
     * 消息ID
     * @return
     */
    public static String getId(){
        return String.valueOf(idWorker.nextId());
    }
}
