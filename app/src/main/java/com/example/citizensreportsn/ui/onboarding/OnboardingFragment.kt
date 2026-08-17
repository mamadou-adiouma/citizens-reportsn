package com.example.citizensreportsn.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.citizensreportsn.R
import com.example.citizensreportsn.databinding.FragmentOnboardingBinding
import com.example.citizensreportsn.databinding.ItemOnboardingSlideBinding

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private lateinit var binding: FragmentOnboardingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingBinding.bind(view)

        val slides = listOf(
            OnboardingSlide(
                "Vous avez repéré un problème ?",
                "Prenez une photo, partagez le lieu et envoyez un signalement en quelques secondes.",
                R.drawable.img_onboarding_1
            ),
            OnboardingSlide(
                "Suivez chaque étape.",
                "Recevez des notifications lorsque votre ticket est examiné, résolu ou fait l'objet d'une réponse.",
                R.drawable.img_onboarding_2
            ),
            OnboardingSlide(
                "Ensemble, faisons la différence.",
                "Votez pour les signalements et commentez-les afin d'aider votre quartier.",
                R.drawable.img_onboarding_3
            )
        )

        val adapter = OnboardingAdapter(slides)
        binding.viewPagerOnboarding.adapter = adapter

        setupIndicators(slides.size)
        setCurrentIndicator(0)

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
                if (position == slides.size - 1) {
                    binding.btnNext.text = "Commencer"
                } else {
                    binding.btnNext.text = "Suivant"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPagerOnboarding.currentItem < slides.size - 1) {
                binding.viewPagerOnboarding.currentItem += 1
            } else {
                findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
            }
        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
        }
    }

    private fun setupIndicators(size: Int) {
        val indicators = arrayOfNulls<ImageView>(size)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            binding.layoutIndicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = binding.layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
}

data class OnboardingSlide(val title: String, val description: String, val imageRes: Int)

class OnboardingAdapter(private val slides: List<OnboardingSlide>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(val binding: ItemOnboardingSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingSlideBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val slide = slides[position]
        holder.binding.apply {
            tvTitle.text = slide.title
            tvDescription.text = slide.description
            ivOnboarding.setImageResource(slide.imageRes)
        }
    }

    override fun getItemCount(): Int = slides.size
}