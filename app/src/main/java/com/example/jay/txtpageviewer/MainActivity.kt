package com.example.jay.txtpageviewer

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.jayfeng.lesscode.core.ActivityLess
import com.jayfeng.lesscode.core.DisplayLess
import com.jayfeng.txtview.TxtView
import com.jayfeng.txtview.page.Page
import com.jayfeng.txtview.page.RenderMode
import com.jayfeng.txtview.touch.PageTouchLinstener
import com.jayfeng.txtview.touch.TouchType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val contentPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#424242")
        textSize = DisplayLess.`$dp2px`(17.0f).toFloat() }

    private val txtViewBuilder: TxtView.Builder by lazy { TxtView.Builder(txtView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityLess.`$fullScreen`(this)
        setContentView(R.layout.activity_main)

        // TxtView Builder
        txtViewBuilder.setRenderMode(RenderMode.DOUBLE_BUFFER)
                .setTitle("我是设置的标题")
                .setBackgroudDrawable(R.drawable.theme_leather_bg)
                .setContentPainter(contentPaint)
                .build()

        val sb = StringBuilder()
        for (i in 1..2) {
            sb.append(content)
            sb.append("\n\n")
        }
        // 设置文本内容
        txtView.postDelayed({
            txtView.setContent(sb.toString(), "22")
        }, 2000)
//        contentView.setTxtFile("")
        // 设置广告图片
        txtView.mAdBitmap = (resources.getDrawable(R.drawable.ad) as BitmapDrawable).bitmap
        // 设置手势回调
        txtView.mPageTouchLinstener = object : PageTouchLinstener {
            override fun onClick(touchType: TouchType, page: Page) {
                Log.d("feng", "touch type: ${touchType.name}")
                when (touchType) {
                    TouchType.AD -> {
                        Toast.makeText(this@MainActivity, "您点击了广告位置", Toast.LENGTH_SHORT).show()
                    }
                    TouchType.LEFT -> {
                        txtView.prevPageWithAnim()
                    }
                    TouchType.RIGHT -> {
                        txtView.nextPageWithAnim()
                    }
                    TouchType.CENTER -> {
                        if (bottom_bars.visibility == View.VISIBLE) {
                            bottom_bars.visibility = View.GONE
                        } else {
                            bottom_bars.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun onLongPressed(touchType: TouchType, page: Page) {
                Toast.makeText(this@MainActivity, "长按： ${touchType.name}", Toast.LENGTH_SHORT).show()
            }
        }

        prevPage.setOnClickListener {
            txtView.prevPageWithAnim()
        }
        nextPage.setOnClickListener {
            txtView.nextPageWithAnim()
        }
        firstPage.setOnClickListener {
            txtView.firstPage()
        }
        lastPage.setOnClickListener {
            txtView.lastPage()
        }

        renderMode.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.renderModeNomal) {
                txtViewBuilder.setRenderMode(RenderMode.NORMAL).build()
            } else {
                txtViewBuilder.setRenderMode(RenderMode.DOUBLE_BUFFER).build()
            }
        }

        nightMode.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.nightModeOn) {
                txtViewBuilder.setNightMode(true).build()
            } else {
                txtViewBuilder.setNightMode(false).build()
            }
        }

        fontBigger.setOnClickListener {
            contentPaint.textSize += 4f
            txtView.scaleFont(contentPaint)
        }
        fontSmaller.setOnClickListener {
            contentPaint.textSize -= 4f
            txtView.scaleFont(contentPaint)
        }

        txtViewLoadingView.setOnClickListener {
            Toast.makeText(this@MainActivity, "点击了 LoadingView", Toast.LENGTH_SHORT).show()
        }

        innerViewLoading.setOnClickListener {
            txtView.showLoading()
            txtView.postDelayed({
                txtView.setContent(sb.toString())
            }, 2000)
        }
        innerViewCustomWithContent.setOnClickListener {
            if (txtView.getCustomView()?.visibility == View.GONE) {
                txtView.showCustomView(false)
            } else {
                txtView.hideCustomView()
            }
        }
        innerViewCustomWithoutContent.setOnClickListener {
            if (txtView.getCustomView()?.visibility == View.GONE) {
                txtView.showCustomView(true)
            } else {
                txtView.hideCustomView()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val startTime = System.currentTimeMillis()
        txtView.saveCache("22")
        Log.d("feng", "------------- save cache cost: " + (System.currentTimeMillis() - startTime))

        txtView.release()
    }

    val content = "　　由于这几年不让下网捕鱼了，水库里的鱼明显多了不少，AAAAABBBBBBBB之后，方圆五六十米水域内的鱼差不多都给震晕浮上了水面，胖子就是闭上眼睛用抄网捞，也是网网都不落空。\n" +
            "　　“汪……汪汪……”\n" +
            "　　就在胖子忙的恨不得多生两只手的时候，远处的村子里传来了一阵狗叫，紧接着几束手电筒的光芒伴着人说话的声音亮了起来，很显然，刚才那动静不大的爆破声，还是惊动了村子。\n" +
            "　　“胖子，有人来了，快点划回来，剩下的鱼不要了……”\n" +
            "　　见到这种情形，三炮连忙压低了声音喊了起来，他知道这几年不比以往了，要是被抓住的话，罚钱都是小事，说不定就会被关上几年的。\n" +
            "　　“没事，他们跑过来还要一会呢……”\n" +
            "　　胖子嘴上说着话，手上也没闲着，用抄网将一条七八斤重的大鲤鱼给捞到了船上，这才往方逸二人所在的岸边划了过来。\n" +
            "　　“接着……”将船划到岸边，胖子先将鱼篓子递了上来，从他用两只手抬鱼篓子的架势上看，一篓子的分量怕是就轻不了。\n" +
            "　　“死胖子，这一篓子都有百十斤了，装这么多也不怕咱们能不能拿回去？”三炮的力气别胖子要小多了，两只手刚一接过篓子，身子就猛地坠了一下，要不是他反应快，恐怕连腰都闪到了。\n" +
            "　　往日里他们扔土炮仗或者下网捕鱼，都会将自行车放在岸边，捞上鱼放在车子上带走，可是今儿哥几个是走过来的，三炮可没那么大力气将鱼给弄回去。\n" +
            "　　“我来吧，咱们赶紧走……”方逸伸手接过了鱼篓子，相比在山上每日都要砍的柴火，这些鱼的重量实在不算什么，两臂一用力，就将两个鱼篓子给拎了起来。\n" +
            "　　“逸哥儿一看就没干过偷鸡摸狗的事情，怕什么啊……”胖子从船上跳上了岸，往亮着灯光的地方看了一眼，说道：“咱们多走几步路，和他们岔开就行了，你放心吧，就算抓到都没事……”\n" +
            "　　村子和水库之间，是一大片玉米地，这会玉米杆已经长到了半人高，稍微矮下一点身子，别说三个人，就是三十个人藏在里面也不显眼。\n" +
            "　　胖子在前面领路，果然是轻而易举的绕过了前来查看动静的人，十多分钟后，他们三个就回到了三炮的家里。\n" +
            "　　“嘿嘿，你们俩坐着，我先去烧条大鲤鱼，让你们哥俩尝尝我的手艺……”进到屋里还没等坐下，胖子就将那条最大的鲤鱼从篓子里给拎了出来，这条鲤鱼很是有些年头了，鱼尾处的鳞片已经变成了红色。\n" +
            "　　“胖子，先等等，你还是先把一篓子鱼给藏屋里去吧……”三炮伸手拦住了胖子。\n" +
            "　　“藏起来干嘛？”胖子闻言愣了一下，说道：“这天也热了，回头吃完了我都用盐给腌起来，不然最多只能放到后天就要坏掉……”\n" +
            "　　小二百斤的鱼，就算是方逸等人放开了肚皮吃，没个十天半月也是吃不完的，胖子早就打算好了，留下几条新鲜的当天吃，剩下的全都给用盐风干后制成腌鱼，挂在屋子里就是一个夏天都坏不了。\n" +
            "　　“腌个屁！”三炮撇了撇嘴，没好气的说道：“胖子，咱俩打个赌不？用不了半小时，你那老子就会找上门来……”\n" +
            "　　“哎，我怎么忘了这茬了……”胖子一拍脑门，苦笑了一声，说道：“得，这一篓子就当是孝敬他们的了，我先把这个篓子给藏起来，省的都被他们给拿走了……”\n" +
            "　　“彭三军，你个臭小子给我出来……”\n" +
            "　　魏大虎来的要比彭三军说的时间还快一些，还没过十分钟，院子外面就响起了魏大虎的喊声，紧接着那木板子做成的院子门，被人一脚给踹开了。\n" +
            "　　“哎呦，魏叔，这半夜的您怎么过来了？”三炮刚一出去，三四个手电筒的光束就打到了他的脸上，用手遮了一下眼睛，三炮看到来人里有个穿制服的，脸色不由变了一下。\n" +
            "　　“你小子少跟我废话，刚才你是不是跑到水库边上炸鱼去了？”魏大虎冷哼了一声，推开三炮就走到了堂屋里。\n" +
            "　　“三叔，我……我们没炸鱼，是……是去钓鱼了……”三炮转身跟了进去，这院子的鱼腥味，他压根就没指望能瞒得住。\n" +
            "　　“六哥，你穿上这身制服，我差点没认出来啊……”进到屋子里之后，三炮才发现穿着制服那人，敢情原来是胖子的堂哥，这心顿时放下去了，笑嘻嘻的掏出烟来，给他和魏大虎各上了一根。\n" +
            "　　“三炮，你小子都是当过兵的人了，回来也不消停点，不知道不让炸鱼了吗？”六哥接过烟，没好气的说道：“这事儿听我三大爷的，他要往派出所交，你们一个都跑不掉……”\n" +
            "　　“六哥，真的是去钓鱼的，可能是别人炸的吧？”虽然魏大虎的儿子也是主犯之一，但架不住魏大虎万一要是大义灭亲呢，所以反正没被抓住现行，三炮这会是死不承认。\n" +
            "　　“不让开山了，除了你们家有火药，谁家还有那玩意儿？”六哥伸手在三炮后脑勺上拍了一巴掌，他心里也有些恼火，因为今儿该他值班，万一事情被捅出去，这联防队的衣服怕是也要被扒掉了。\n" +
            "　　“华子，你小子给我滚出来……”魏大虎没搭理三炮，而是冲里屋喊了一嗓子，他知道儿子的秉性，这事儿要是没他才怪了，说不定自己儿子还是主谋呢。\n" +
            "　　“爸，哥，四叔……”\n" +
            "　　魏大虎话声未落，胖子就从里屋钻了出来，一看来的几个人，那心也是放下去了，除了老爸堂哥之外，剩下的那个人是自己的亲四叔，胖子才不信他们会把自己送进派出所呢。\n" +
            "　　“臭小子，正经事不干，这歪门邪道你倒是跑得快啊……”见到儿子，魏大虎是气不打一处来，虽说早些年自家也炸鱼，但现在不是不允许了嘛，作为村长，他还是要以身作则的。\n" +
            "　　“爸，这不是方逸下山了，我们搞点鱼给他吃嘛……”\n" +
            "　　屋里都是自家人，胖子也不害怕，笑嘻嘻的说道：“搞了有七八十斤，我们留一条，其他的你们都拿走，还别说，这水库里的鱼是越来越肥了……”\n" +
            "　　“魏叔，这个……”\n" +
            "　　听胖子提到自己的名字，方逸也不好意思藏在屋里了，挠着头走了出来，开口说道：“魏叔，给你添麻烦了，要……要不这些鱼算我们买的吧，师父给我还留了点儿钱……”\n" +
            "　　“得了吧，你师父有钱都买酒了，能给你留多少？”\n" +
            "　　魏大虎摆了摆手，不过看到方逸，他的语气也缓和了下来，毕竟村子里的人都承过老道士的情分，当年老道士给人看病，可是连草药钱都没收过的。\n" +
            "　　“咳，留……留了一百多……”\n" +
            "　　魏大虎的话让方逸愈发的不好意思了，正如魏大虎说的那样，老道士每顿饭几乎是无酒不欢，猴儿酒不够喝的，他就到山下买酒，身后也就留给了方逸一百多块钱。\n" +
            "　　“方逸，我看你还是正经做点事吧……”"
}
