package cn.a10miaomiao.bilimiao.compose.pages.lyric.lib;

import java.io.IOException;

//https://blog.csdn.net/qingzi635533/article/details/30231733  此处源码已修改
public class KrcText
{
    private static final char[] miarry = { '@', 'G', 'a', 'w', '^', '2', 't',
            'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i' };
    /**
     *
     * @param zip_byte 去掉前四字节“krc1”后的部分
     * @return krc文件处理后的文本
     * @throws IOException
     */
    public String getKrcText(byte[] zip_byte) throws IOException
    {
        int j = zip_byte.length;
        for (int k = 0; k < j; k++)
        {
            int l = k % 16;
            zip_byte[k] = (byte) (zip_byte[k] ^ miarry[l]);
        }
        return new String(ZLibUtils.decompress(zip_byte), "utf-8");
    }
}