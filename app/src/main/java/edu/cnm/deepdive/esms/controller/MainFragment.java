package edu.cnm.deepdive.esms.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.tabs.TabLayoutMediator;
import edu.cnm.deepdive.esms.NavigationGraphDirections;
import edu.cnm.deepdive.esms.R;
import edu.cnm.deepdive.esms.adapter.VPAdapter;
import edu.cnm.deepdive.esms.databinding.FragmentMainBinding;
import edu.cnm.deepdive.esms.model.entity.Species;
import edu.cnm.deepdive.esms.model.entity.User;
import edu.cnm.deepdive.esms.viewmodel.SpeciesViewModel;
import edu.cnm.deepdive.esms.viewmodel.UserViewModel;
import java.util.Collection;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;

public class MainFragment extends Fragment implements OnItemSelectedListener {

  private FragmentMainBinding binding;
  private VPAdapter vpAdapter;
  private final String[] titles = new String[]{"Species", "Team", "Evidence"};
  private UserViewModel userViewModel;
  private SpeciesViewModel speciesViewModel;
  private NavController navController;
  private User currentUser;
  private Species species;
  private ArrayAdapter<Species> adapter;
  private ViewModelProvider provider;

  @Override
  public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    vpAdapter = new VPAdapter(this);
    binding.viewPager.setAdapter(vpAdapter);
    new TabLayoutMediator(binding.tabLayout, binding.viewPager,
        (tab, position) -> tab.setText(titles[position])).attach();
    binding.casesSpinner.setOnItemSelectedListener(this);
    adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.casesSpinner.setAdapter(adapter);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull @NotNull View view,
      @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupViewModel();
    navController = Navigation.findNavController(binding.getRoot());
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu,
      @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.main_options, menu);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull @NotNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (currentUser != null) {
      menu.findItem(R.id.admin)
          .setVisible(currentUser.getRoles().contains("ADMINISTRATOR"));
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    boolean handled;
    int itemId = item.getItemId();
    if (itemId == R.id.sign_out) {
      userViewModel.signOut();
      handled = true;
    } else if (itemId == R.id.settings) {
      navController.navigate(NavigationGraphDirections.openSettings());
      handled = true;
    } else if (itemId == R.id.admin) {
      navController.navigate(NavigationGraphDirections.openAdminMenu());
      handled = true;
    } else {
      handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  private void setupViewModel() {
    FragmentActivity activity = getActivity();
    provider = new ViewModelProvider(activity);
    userViewModel = provider.get(UserViewModel.class);
    LifecycleOwner owner = getViewLifecycleOwner();
    userViewModel
        .getCurrentUser()
        .observe(owner, (user) -> {
          currentUser = user;
          activity.invalidateOptionsMenu();
        });
    speciesViewModel = provider.get(SpeciesViewModel.class);
    speciesViewModel
        .getSpecies()
        .observe(owner, (species) -> {
          this.species = species;
          updateSpinnerSelection();
        });
    speciesViewModel
        .getSpeciesList()
        .observe(owner, (speciesIterable) -> {
          adapter.clear();
          adapter.addAll(speciesIterable);
          adapter.notifyDataSetChanged();
          updateSpinnerSelection();
        });
    speciesViewModel.fetchSpeciesList();
  }

  private void updateSpinnerSelection() {
    if (species != null && species.getId() != null && !adapter.isEmpty()) {
      Species selected = (Species) binding.casesSpinner.getSelectedItem();
      if (selected != null && !species.getId().equals(selected.getId())) {
        binding.casesSpinner.setSelection(adapter.getPosition(species));
      } else {
        binding.casesSpinner.setSelection(adapter.getPosition(species), true);
      }
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int position, long longId) {
    Species species = (Species) adapterView.getItemAtPosition(position);
    speciesViewModel.setSpecies(species);
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

    speciesViewModel.setSpecies(null);
  }
}