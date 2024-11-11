package com.julic20s.fleur.data

import com.julic20s.fleur.R

object DefaultPlantsRepository : PlantsRepository {

    private val _plants = listOf(
        PlantInfo(
            id = 0,
            name = "牡丹",
            type = "芍药科芍药属",
            res = R.drawable.md,
            description = "芍药在东方文化里占据着非常重要的地位，中国最早的一部诗歌总集《诗经》" +
                    "里就出现了芍药的影子——“维士与女，伊其相谑，赠之以芍药”，描述的是春季出游的青年男女互相爱慕，" +
                    "通过赠送芍药来表达自己的心意，芍药也自此成为中国古代的定情之花。\n" +
                    "在欧洲，芍药的栽培也有很长的历史，但是最开始只是用作食物和药物，芍药根被看作是烤肉时可以添加的上佳佐料。" +
                    "在十四世纪，威廉·兰格伦（William Langland）的著名长诗《农夫皮尔斯》中，芍药和胡椒粉一起被人们当做调味品对待。\n" +
                    "1805年，英国探险家、博物学家约瑟夫·班克斯爵士（Sir Joseph Banks）陆续将几个中国芍药品种引入英国，受到了英" +
                    "国园艺界和上流社会的热烈追捧。很多画家也为之着迷，雷诺阿、莫奈、梵高等伟大的画家都画过芍药，芍药也由此进一步被大众了解和喜爱。",
            info = mapOf(
                "尺寸" to "50~70 厘米",
                "耐寒" to "USDA耐寒区2-9",
                "光照" to "全日照",
                "土壤" to "肥沃的排水良好的土壤，pH 6.0~7.0",
                "水分" to "除非气候过于干燥，否则无需额外浇水",
                "肥料" to "每年三次，施肥的时间也很关键",
            )
        ),
        PlantInfo(
            id = 1,
            name = "郁金香",
            type = "百合科郁金香属",
            res = R.drawable.yjx,
            description = "郁金香（Tulipa gesneriana）是最受欢迎的球根花卉，也是春天的代表性花卉之一。 它们是多年生草本植物，" +
                    "以鳞茎过冬，花大而艳丽，花色多样，其中红色、黄色、白色和粉色的花最为常见。它们含苞待放的花朵高雅且颇具异国情调，" +
                    "自16世纪末传入欧洲后就一直广受喜爱。",
            info = mapOf(
                "尺寸" to "随品种不同，可能高10-72厘米不等",
                "光照" to "全日照至半荫蔽",
                "土壤" to "排水良好且肥沃的砂质土壤",
                "开花时间" to "春季",
            ),
        ),
        PlantInfo(
            id = 2,
            name = "月季",
            type = "蔷薇科蔷薇属",
            res = R.drawable.yj,
            description = "",
            info = mapOf(
                "尺寸" to "从微型月季到藤本月季，大小随品种不同变化很大",
                "耐寒性" to "USDA耐寒区间5-9",
                "光照" to "全日照",
                "土壤" to "肥沃且排水良好的土壤，pH 6.0-7.0",
                "开花时间" to "春季至秋季",
            ),
        ),
        PlantInfo(
            id = 3,
            name = "丁香",
            type = "木犀科丁香属",
            res = R.drawable.dx,
            description = "",
            info = mapOf(
                "尺寸" to "2-5米高，2.5-4.5米宽",
                "耐寒" to "USDA耐寒区3-7",
                "光照" to "全日照",
                "土壤" to "中性、排水良好、肥沃的壤质土",
                "开花时间" to "春季",
            ),
        ),
        PlantInfo(
            id = 4,
            name = "山茶花",
            type = "山茶科山茶属",
            res = R.drawable.sc,
            description = "",
            info = mapOf(
                "尺寸" to "一般2-5米（ 6-15英尺）高",
                "耐寒" to "USDA耐寒区间7-10",
                "光照" to "半阴",
                "土壤" to "潮湿但排水良好，微酸性",
                "开花时间" to "深秋至早春（品种间有区别）",
            ),
        ),
        PlantInfo(
            id = 5,
            name = "六出花",
            type = "六出花科六出花属",
            res = R.drawable.lc,
            description = "",
            info = mapOf(),
        ),
        PlantInfo(
            id = 6,
            name = "马蹄莲",
            type = "天南星科马蹄莲属",
            res = R.drawable.mtl,
            description = "",
            info = mapOf(),
        ),
        PlantInfo(
            id = 7,
            name = "夏蜡梅",
            type = "蜡梅科夏蜡梅属",
            res = R.drawable.xlm,
            description = "",
            info = mapOf(),
        ),
    )

    override val plants: List<PlantInfo> get() = _plants

    override fun getPlant(id: Int): PlantInfo? {
        if (id < 0 || _plants.size <= id) {
            return null
        }
        return _plants[id]
    }
}